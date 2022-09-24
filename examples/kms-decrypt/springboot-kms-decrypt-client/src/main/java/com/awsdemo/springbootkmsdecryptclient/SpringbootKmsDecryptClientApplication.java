package com.awsdemo.springbootkmsdecryptclient;

import com.github.mrgatto.autoconfigure.EnableNitroEnclavesHostSide;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.awsdemo.springbootkmsdecryptclient" })
@EnableNitroEnclavesHostSide
public class SpringbootKmsDecryptClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootKmsDecryptClientApplication.class, args);
    }

}
