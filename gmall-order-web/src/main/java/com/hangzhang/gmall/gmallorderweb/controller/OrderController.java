package com.hangzhang.gmall.gmallorderweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.OmsCartItem;
import com.beans.OmsOrder;
import com.beans.OmsOrderItem;
import com.beans.UmsMemberReceiveAddress;
import com.hangzhang.gmall.annotations.LoginRequired;
import com.service.CartService;
import com.service.OrderService;
import com.service.SkuService;
import com.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;


    @RequestMapping("submitOrder")
    //标识 只传递地址
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //检查交易码（redis设置过期时间）
        String  check = orderService.checkTradeCode(memberId,tradeCode);

        if(check.equals("success")){

            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);//自动确认收货时间
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            String outTradeNo="gmall";
            outTradeNo = outTradeNo+System.currentTimeMillis();

            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);

            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);

            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            omsOrder.setReceiveTime(c.getTime());
            omsOrder.setSourceType(1);//PC  APP
            omsOrder.setOrderType(1);//订单状态

            //依据用户id获得要购买的商品列表（购物车），总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem.getIsChecked().equals("1")){
                    //获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //生成订单时检验价格和检验库存（双十一一瞬间改价格的时候）
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b==false){
                        //验价失败，不替用户做决定，只要订单中一个商品没有了，结算失败，返回订单
                        ModelAndView modelAndView = new ModelAndView("tradeFail");
                        return modelAndView;
                    }

                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());


                    omsOrderItem.setOrderSn(outTradeNo);//订单号（外部订单号，防重复）
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("11111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应ID");


                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);


            //远程调用库存系统

            //将订单和订单详情写入数据库，并删除购物车
            orderService.saveOrder(omsOrder);

            //重定向到支付系统
            ModelAndView modelAndView = new ModelAndView("redirect:http://payment.gmall.com:8088/index");
            //有篡改风险，完善的系统应该通过用户ID在支付服务里面进行查询
            modelAndView.addObject("outTradeNo",outTradeNo);
            modelAndView.addObject("totalAmount",totalAmount);
            return modelAndView;

        }else{
            ModelAndView modelAndView = new ModelAndView("tradeFail");
            return modelAndView;
        }



    }

    @RequestMapping("toTrade")
    //标识
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //用户地址列表集合
        System.out.println(memberId);
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);
        //将购物车集合转化为页面结算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            //每循环一个购物车对象，就封装一个商品的详情到omsOrderItems
            if(omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItems.add(omsOrderItem);
            }

        }
        BigDecimal totalAmount = gettotalAmount(omsCartItems);
        modelMap.put("orderDetailList",omsOrderItems);
        modelMap.put("userAddressList",receiveAddressByMemberId);
        modelMap.put("totalAmount",totalAmount);

        //生成交易码，进行校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal gettotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal bigDecimal = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getPrice().multiply(omsCartItem.getQuantity());
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal = bigDecimal.add(totalPrice);
            }

        }

        return bigDecimal;
    }
}
