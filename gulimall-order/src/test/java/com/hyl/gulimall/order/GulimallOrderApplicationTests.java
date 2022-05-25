package com.hyl.gulimall.order;

import com.hyl.gulimall.order.feign.MemberFeignService;
import com.hyl.gulimall.order.vo.MemberAddressVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
//import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    MemberFeignService memberFeignService;


    @Test
    public void testMemberService(){
        List<MemberAddressVo> address = memberFeignService.getAddress(2l);
        if(address!=null&&address.size()>0) {
            for(MemberAddressVo a:address) {
                System.out.println(a);
            }
        }
    }

}
