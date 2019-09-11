package com.hangzhang.gmall.gmallcartweb.controller;

import java.math.BigDecimal;

public class TestBigDecimal {
    public static void  main(String args[]){
        //初始化(丢失精度的问题) 幂函数表示精度丢失
        BigDecimal bigDecimal1 = new BigDecimal(0.01f);//略小
        BigDecimal bigDecimal2 = new BigDecimal(0.01d);//略大
        BigDecimal bigDecimal3 = new BigDecimal("0.01");//相等
        System.out.println(bigDecimal1);
        System.out.println(bigDecimal2);
        System.out.println(bigDecimal3);

        //比较
        int i = bigDecimal1.compareTo(bigDecimal2);//1 -1 0
        System.out.println(i);
        
        //运算
        BigDecimal add = bigDecimal1.add(bigDecimal2);
        BigDecimal subtract = bigDecimal2.subtract(bigDecimal3);
        BigDecimal bigDecimal4 = new BigDecimal("6");//略大
        BigDecimal bigDecimal5 = new BigDecimal("7");//相等
        BigDecimal multiply = bigDecimal4.multiply(bigDecimal5);
        BigDecimal divide = bigDecimal4.divide(bigDecimal5,3,BigDecimal.ROUND_CEILING);//不会自动约等于，不会自动取整
        System.out.println(add);
        System.out.println(subtract);
        System.out.println(multiply);
        System.out.println(divide);

        //约束 除了除法以外的方法
        BigDecimal add1 = bigDecimal2.add(bigDecimal3);
        BigDecimal bigDecimal = add1.setScale(3, BigDecimal.ROUND_DOWN);//保留三位并四舍五入
        System.out.println(bigDecimal);
    }
}
