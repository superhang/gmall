package com.hangzhang.gmall.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.beans.OmsCartItem;
import com.beans.PmsSkuInfo;
import com.hangzhang.gmall.annotations.LoginRequired;
import com.hangzhang.gmall.util.CookieUtil;
import com.service.CartService;
import com.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    CartService cartService;
    @Reference
    SkuService skuService;

    @RequestMapping("toTrade")
    //标识
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        return "toTrade";
    }

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked,String skuId, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);

        //调用服务，修改状态
        cartService.checkCart(omsCartItem);
        //将最新的数据从缓存中拿出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        //结算时的价格
        BigDecimal totalAmount = gettotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList( HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        if(StringUtils.isNotBlank(memberId)){
            //已经登录查询数据库（缓存查询）
            omsCartItems = cartService.cartList(memberId);
        }else{
            //没有登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList",omsCartItems);
        //结算时的价格
        BigDecimal totalAmount = gettotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);

        return "cartList";
    }

    private BigDecimal gettotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal bigDecimal = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal = bigDecimal.add(totalPrice);
            }

        }

        return bigDecimal;
    }

    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response){
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, "127.0.0.1");
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("1111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(num));
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //判断用户是否登录,获取用户ID
        //String memberId = "8";//request.getAttribute("memberId");
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //决定走cookie还是db
        /**
         * Db:cartListDb 有主键和用户id
         * Cookie:cartListCookie 没有主键和用户id
         * Redis：cartListCache 有主键和用户id
         * */
        if(StringUtils.isNotBlank(memberId)){
            //DB+redis

            OmsCartItem omsCartItemnew = cartService.ifCartExitsByUser(memberId,skuId);

            if(omsCartItemnew==null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("testSuperHang");
                omsCartItem.setQuantity(new BigDecimal(num));
                cartService.addCart(omsCartItem);
            }else{
                //该用户添加过当前商品
                omsCartItemnew.setQuantity(omsCartItemnew.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemnew);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }else{
          //Cookie
            //更新cookie 原有的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的购物车信息是否已经存在
                boolean exist = if_cart_exit(omsCartItems,omsCartItem);
                if(exist){
                    //更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                }else
                {
                    //添加一条新记录
                    omsCartItems.add(omsCartItem);
                }
            }else{
                //cookie为空
                omsCartItems.add(omsCartItem);
            }

            //覆盖cookie
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);
        }

        //必须放到static目录下才可重定向向访问
        return "redirect:/success.html";
    }

    private boolean if_cart_exit(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean falg = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productId = cartItem.getProductSkuId();
            if(productId.equals(omsCartItem.getProductSkuId())){
                falg = true;
            }
        }
        return falg;
    }
}
