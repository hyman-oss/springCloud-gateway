package com.hyzs.dog.gateway.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 断路器配置
 * @author Hua-cloud
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public Map<String,Object> fallback(){
        Map<String,Object> p = new HashMap<>(16);
        p.put("code", "-100");
        p.put("data", "service not available");
        return p;
    }
}
