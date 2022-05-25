package com.hyl.gulimall.member;


import com.hyl.gulimall.member.entity.MemberReceiveAddressEntity;
import com.hyl.gulimall.member.service.MemberReceiveAddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Autowired
    MemberReceiveAddressService memberReceiveAddressService;


    @Test
    public void testGetAddress(){
        List<MemberReceiveAddressEntity> address = memberReceiveAddressService.getAddress(2L);
        if(address!=null&&address.size()>0) {
            for(MemberReceiveAddressEntity a:address) {
                System.out.println(a);
            }
        }

    }


}
