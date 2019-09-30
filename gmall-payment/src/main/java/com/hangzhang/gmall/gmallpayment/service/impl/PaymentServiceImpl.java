package com.hangzhang.gmall.gmallpayment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.beans.PaymentInfo;
import com.hangzhang.gmall.gmallpayment.mapper.PaymentInfoMapper;
import com.hangzhang.gmall.mq.ActiveMQUtil;
import com.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updataPayment(PaymentInfo paymentInfo) {
        //进行支付更新的幂等性检查操作
        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfo2 = paymentInfoMapper.selectOne(paymentInfo1);
        if(StringUtils.isNotBlank(paymentInfo2.getPaymentStatus())&&paymentInfo2.getPaymentStatus().equals("已支付")){
            return;
        }else{
            String orderSn = paymentInfo.getOrderSn();
            Example e = new Example(PaymentInfo.class);
            e.createCriteria().andEqualTo("orderSn",orderSn);
            Connection connection = null;
            Session session = null;
            try {
                connection = activeMQUtil.getConnectionFactory().createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException ex) {
                ex.printStackTrace();
            }

            try{
                paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
                //支付成功后引起的其它服务的更新 （订单服务的服务更新-》库存服务-》物流服务 事务回滚）
                //调用mq发送支付成功消息  （这里保证消息的一致性）
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);

                //信息结构  字符串文本  hash结构
                //TextMessage textMessage = new ActiveMQTextMessage();
                MapMessage mapMessage = new ActiveMQMapMessage();
                mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

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

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo,int count) {
        /**
         *
         * 在提交支付后，向消息队列发送一个延迟执行的消息任务，当改任务被支付服务执行时，
         * 在消费任务的程序中去查询当前交易状态，依据交易状态（支付结束）决定解除延迟任务还是继续再设置新
         * 的延迟任务。
         *
         * 配置消息队列的延迟属性  schedulerSupport="true"
         *
         * */


        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

        try{
            Queue payment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            //信息结构  字符串文本  hash结构
            //TextMessage textMessage = new ActiveMQTextMessage();
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",outTradeNo);
            mapMessage.setInt("count",count);
            //加入延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);
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
    @Value("${app_id}") private String app_id;
    @Value("${app_private_key}") private String app_private_key;
    @Value("${alipay_public_key}") private String alipay_public_key;

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        Map<String,Object> resultMap = new HashMap<>();

        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
                app_id,app_private_key,"json","GBK",alipay_public_key,"RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response!=null && response.isSuccess()){
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("call_back_content",response.getMsg());
            System.out.println("调用成功");
        } else {
            System.out.println("(有可能交易未创建)调用失败");
        }
        return resultMap;
    }
}
