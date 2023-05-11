package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DishService dishService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据，{}", shoppingCart.toString());
        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //如果已存在，就在原来数量基础上加一
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (dishId != null) {
            Dish dish = dishService.getById(dishId);
            Integer dishCount = dish.getDishCount();
            if (dishCount < (cartServiceOne == null ? 0 : cartServiceOne.getNumber()) + 1) {
                return R.error(dish.getName() + "数量不足");
            }
        }

        if (cartServiceOne != null) {
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，则添加到购物车，默认数量为一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrap = new LambdaQueryWrapper<>();
        queryWrap.eq(currentId != null, ShoppingCart::getUserId, currentId);

        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null) {
            queryWrap.eq(ShoppingCart::getSetmealId, setmealId);

        } else {
            Long dishId = shoppingCart.getDishId();
            queryWrap.eq(dishId != null, ShoppingCart::getDishId, dishId);
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrap);

        Integer number = cartServiceOne.getNumber();
        if (number == 1) {
            shoppingCartService.removeById(cartServiceOne);
        } else {
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }

        return R.success(cartServiceOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrap = new LambdaQueryWrapper<>();
        queryWrap.eq(currentId != null, ShoppingCart::getUserId, currentId);
        queryWrap.gt(ShoppingCart::getNumber, 0);
        queryWrap.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrap);
        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> delete() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrap = new LambdaQueryWrapper<>();
        queryWrap.eq(currentId != null, ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(queryWrap);
        return R.success("清空购物车成功");
    }
}
