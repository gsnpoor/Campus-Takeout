package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redission;

    /*
     * 用户下单
     * */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) throws InterruptedException {
        log.info("订单数据：{}", orders.toString());
        Long userId = BaseContext.getCurrentId();

        //尝试获取锁20s,获取失败则返回
        RLock lock = redission.getLock("dish_*");
        boolean requireLock = lock.tryLock(20, TimeUnit.SECONDS);
        if (!requireLock) {
            return R.error("获取锁超时");
        }
        try {
            //查询当前用户的购物车数据
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId, userId);
            List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

            for (ShoppingCart shoppingCart : shoppingCarts) {
                Dish dish = dishService.getById(shoppingCart.getDishId());
                Integer dishCount = dish.getDishCount();
                if (dishCount < 1) {
                    return R.error(dish.getName() + "数量不足，购买失败");
                }
            }
            ordersService.submit(orders);

            //清理所有菜品的缓存数据
            Set keys = redisTemplate.keys("dish_*");
            redisTemplate.delete(keys);

            return R.success("下单成功");
        }catch (Exception e) {
            e.printStackTrace();
            return R.error("程序出错");
        }finally {
            lock.unlock();
        }
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);


        return R.success(pageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number != null, Orders::getNumber, number);
        queryWrapper.between(beginTime != null, Orders::getOrderTime, beginTime, endTime);

        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping("/status")
    public R<String> updateStatus(@RequestBody Orders order){
        Orders orders = ordersService.getById(order.getId());
        orders.setStatus(order.getStatus());
        ordersService.updateById(orders);
        return R.success("派送成功");
    }
}
