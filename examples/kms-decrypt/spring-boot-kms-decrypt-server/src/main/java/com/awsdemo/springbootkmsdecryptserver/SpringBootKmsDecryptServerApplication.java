package com.awsdemo.springbootkmsdecryptserver;

import com.github.mrgatto.autoconfigure.EnableNitroEnclavesEnclaveSide;
import com.github.mrgatto.enclave.server.NitroEnclaveServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.awsdemo.springbootkmsdecryptserver" })
@EnableNitroEnclavesEnclaveSide
public class SpringBootKmsDecryptServerApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringBootKmsDecryptServerApplication.class, args);
        NitroEnclaveServer server = ctx.getBean(NitroEnclaveServer.class);
        server.run();
    }

}
