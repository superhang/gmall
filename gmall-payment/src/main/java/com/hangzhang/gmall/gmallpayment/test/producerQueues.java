package com.hangzhang.gmall.gmallpayment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class producerQueues {
    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启事务
            Queue testqueue = session.createQueue("drink");

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("我渴了，谁能帮我打一杯水");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//持久化 一直有效
            producer.send(textMessage);
            session.commit();//提交事务
            connection.close();//关闭连接

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
