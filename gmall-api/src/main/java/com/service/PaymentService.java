package com.service;

import com.beans.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updataPayment(PaymentInfo paymentInfo);
}
