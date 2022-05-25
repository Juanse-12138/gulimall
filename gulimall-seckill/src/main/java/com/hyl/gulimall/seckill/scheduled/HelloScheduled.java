package com.hyl.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class HelloScheduled {
//    @Scheduled(cron="* * * * * ?")
//    public void hello() {
//        log.info("hello");
//    }
}
