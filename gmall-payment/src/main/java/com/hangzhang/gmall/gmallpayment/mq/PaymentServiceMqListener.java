package com.hangzhang.gmall.gmallpayment.mq;

import com.beans.PaymentInfo;
import com.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    //消息名   工具类注入的mq容器
    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        int count = Integer.valueOf(mapMessage.getString("count"));

        //调用paymentService的支付宝检查接口 查不到支付状态的原因
        Map<String,Object> resultMap =  paymentService.checkAlipayPayment(out_trade_no);
        if(resultMap==null || resultMap.isEmpty()){
            //继续发送延迟检查任务，计算延迟时间
            if(count>0){
                System.out.println("没有支付成功，继续发送延迟检查任务,检查剩余次数为 "+ count);
                count--;
                paymentService.sendDelayPaymentResultCheckQueue(out_trade_no,count);

            }else {
                System.out.println("检查剩余次数用尽，结束检查");
            }


        }else{
            String trade_status = (String)resultMap.get("trade_status");

            //依据支付状态结果，判断是否进行下一次的延迟任务还是支付成功更新数据和后续任务
            if(StringUtils.isNotBlank(trade_status) && trade_status.equals("TRADE_SUCCESS")){
                //支付成功，更新支付发送支付队列

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));
                paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));
                paymentInfo.setCallbackTime(new Date());

                paymentService.updataPayment(paymentInfo);
                System.out.println("已经支付成功，调用支付服务，修改支付信息和发送支付成功的队列");
                return;
            }else{
                //继续发送延迟检查任务，计算延迟时间
                if(count>0){
                    System.out.println("没有支付成功，继续发送延迟检查任务,检查剩余次数为 "+ count);
                    count--;
                    paymentService.sendDelayPaymentResultCheckQueue(out_trade_no,count);

                }else {
                    System.out.println("检查剩余次数用尽，结束检查");
                }
            }
        }

    }
}
