package com.hangzhang.gmall.gmallpayment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.PaymentInfo;
import com.hangzhang.gmall.gmallpayment.mapper.PaymentInfoMapper;
import com.hangzhang.gmall.mq.ActiveMQUtil;
import com.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updataPayment(PaymentInfo paymentInfo) {
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
