package com.itheima.reggie.config;

import com.google.common.hash.BloomFilter;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class InitConfig implements InitializingBean {
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisConfig redisConfig;
    @Autowired
    @Qualifier("userPhoneBloom")
    BloomFilter<String> userPhoneBloom;
    /**
     * 把手机号加载到 Redis中
     */
    @Override
    //设置任务执行的间隔时间(1天)
    @Scheduled(fixedRate = 1000*60*60*24)
    public void afterPropertiesSet() throws Exception {
        log.info("初始化布隆过滤器");
        //缓存数据库中所有的手机号
        List<User> users = userService.list();

        if (users == null) {
            log.info("暂无此用户");
            return;
        }
        //将用户手机号存入redis中
        List<String> userPhoneList = new ArrayList<>();
        for (User user : users) {
            userPhoneList.add(user.getPhone());
            //存储值到布隆过滤器中
            userPhoneBloom.put(user.getPhone()+"");
        }
        redisTemplate.opsForValue().set("userPhoneList:", userPhoneList,1,TimeUnit.DAYS);
        log.info("初始化布隆过滤器成功");
    }
}
