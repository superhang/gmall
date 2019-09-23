package com.service;

import com.beans.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);

    String genTradeCode(String memberId);


    void saveOrder(OmsOrder omsOrder);
}
