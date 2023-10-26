package com.fgama.pillowtalk;

import com.fgama.pillowtalk.service.ChattingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class PillowtalkApplication {

    static {
        System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true");
    }

    @Autowired
    ChattingRoomService chattingRoomService;

    public static void main(String[] args) {
        SpringApplication.run(PillowtalkApplication.class, args);
    }

}
