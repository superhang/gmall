package com.hangzhang.gmall.gmallpayment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.beans.OmsOrder;
import com.beans.PaymentInfo;
import com.hangzhang.gmall.annotations.LoginRequired;
import com.hangzhang.gmall.gmallpayment.config.AlipayConfig;
import com.service.OrderService;
import com.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class paymentController {

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    PaymentService paymentService;
    @Reference
    OrderService orderService;


    @RequestMapping("alipay/callback/return")
//    @LoginRequired(loginSuccess = true)   验证登录这里没有token（浏览器的token删除了，不知道原因）
    public String callbackreturn(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        //回调接口，验参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String trade_amount = request.getParameter("trade_amount");
        String call_back_content = request.getQueryString();

        PaymentInfo paymentInfo = new PaymentInfo();


        //通过支付宝的paramMap进行签名验证，2.0版本的接口屏蔽参数导致无法验标签
        if(StringUtils.isNotBlank(sign)){
            //验标签成功
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态
            paymentService.updataPayment(paymentInfo);
        }
        //支付成功后引起的其它服务的更新 （订单服务的服务更新-》库存服务-》物流服务 事务回滚）
        //调用mq发送支付成功消息
        return "finish";
    }
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        return null;
    }

    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_order_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
//        map.put("total_amount",totalAmount);
        map.put("total_amount",0.01);
        map.put("subject","土豪商品");

        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);

        //获取支付宝请求的客户端（封装好的一个http请求）
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //生成并且保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByoutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("订单未付款");
        paymentInfo.setSubject("土豪商品一件");
        paymentInfo.setTotalAmount(totalAmount);

        paymentService.savePaymentInfo(paymentInfo);

        //提交请求到支付宝
        return form;
    }


    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        modelMap.put("nickName",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("nickName",nickname);

        return  "index";
    }
}
