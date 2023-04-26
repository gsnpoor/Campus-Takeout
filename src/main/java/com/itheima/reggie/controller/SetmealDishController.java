package com.itheima.reggie.controller;

import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 套餐管理
* */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealDishController {
    @Autowired
    private SetmealService setmealDish;

    @Autowired
    private SetmealDishService setmealDishService;
}
