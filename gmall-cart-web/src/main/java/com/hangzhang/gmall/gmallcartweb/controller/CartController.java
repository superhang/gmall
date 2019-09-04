package com.hangzhang.gmall.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.beans.OmsCartItem;
import com.beans.PmsSkuInfo;
import com.hangzhang.gmall.util.CookieUtil;
import com.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
//    @Reference
//    CartService cartService;
    @Reference
    SkuService skuService;

    @RequestMapping("addToCart")
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response){
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, "");
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
        omsCartItem.setQuantity(num);

        //判断用户是否登录
        String memberId = "";

        //决定走cookie还是db
        /**
         * Db:cartListDb 有主键和用户id
         * Cookie:cartListCookie 没有主键和用户id
         * Redis：cartListCache 有主键和用户id
         * */
        if(StringUtils.isNotBlank(memberId)){
          //DB+redis
        }else{
          //Cookie
            List<OmsCartItem> omsCartItems = new ArrayList<>();

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
                            cartItem.setQuantity(cartItem.getQuantity()+omsCartItem.getQuantity());
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
