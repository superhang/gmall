package com.hangzhang.gmall.gmallorderservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.beans.OmsOrder;
import com.beans.OmsOrderItem;
import com.hangzhang.gmall.gmallorderservice.mapper.OmsOrderItemMapper;
import com.hangzhang.gmall.gmallorderservice.mapper.OmsOrderMapper;
import com.hangzhang.gmall.mq.ActiveMQUtil;
import com.hangzhang.gmall.util.RedisUtil;
import com.service.CartService;
import com.service.OrderService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;


    //远程注入购物车服务
    @Reference
    CartService cartService;

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            String tradeKey = "user:"+memberId+":tradeCode";
            String tradeCodeFromCache = jedis.get(tradeKey);
            //多个用户同时提交订单的bug
            //redis lua脚本防止1key多用  防止黑客并发订单攻击
            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),Collections.singletonList(tradeCode));


            if(eval!=null && eval!=0){
                //删除缓存里面的交易码
                //jedis.del(tradeKey);

                return "success";
            }else{
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:"+memberId+":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.setex(tradeKey,60*15,tradeCode);

        jedis.close();
        return tradeCode;
    }



    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        //保存订单详细
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车记录 依据订单号找到对应用户和商品
            //cartService.del();
        }

    }

    @Override
    public OmsOrder getOrderByoutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        OmsOrder omsOrder1 = new OmsOrder();
        omsOrder1.setStatus("1");
        omsOrder1.setOrderSn(omsOrder.getOrderSn());


        //发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        try{
            //更新订单状态
            omsOrderMapper.updateByExampleSelective(omsOrder1,e);

            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //信息结构  字符串文本  hash结构
            //TextMessage textMessage = new ActiveMQTextMessage();
            MapMessage mapMessage = new ActiveMQMapMessage();
//            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

            producer.send(mapMessage);
            session.commit();
        }catch (Exception e1){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }finally {
                try {
                    connection.close();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


}
