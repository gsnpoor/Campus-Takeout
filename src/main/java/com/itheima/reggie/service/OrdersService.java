package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {
    /*
    * 用户下单
    * */
    public void submit(Orders orders);
}
