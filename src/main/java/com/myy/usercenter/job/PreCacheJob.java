package com.myy.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myy.usercenter.model.domain.User;
import com.myy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Slf4j
@Component
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1l);

    //每天执行，预热推荐用户
    @Scheduled(cron = "21 52 21 * * *")
    public void doCacheRecommendUser(){
        for (Long userId: mainUserList){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
            String redisKey = String.format("friends:user:recommend:%s", userId);
            ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
            //写缓存
            try {
                valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                System.out.println("aaaaa");
            }catch (Exception e){
                log.error("redis set key error",e);
            }
        }
    }

}
