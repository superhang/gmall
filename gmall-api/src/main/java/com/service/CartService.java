package com.service;

import com.beans.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifCartExitsByUser(String memberId,String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemnew);

    void flushCartCache(String memberId);


    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);
}
