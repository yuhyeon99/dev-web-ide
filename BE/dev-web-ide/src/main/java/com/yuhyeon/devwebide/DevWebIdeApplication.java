package com.yuhyeon.devwebide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 (생성일, 수정일 자동 관리를 위함)
 */
@EnableJpaAuditing
@SpringBootApplication
public class DevWebIdeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevWebIdeApplication.class, args);
    }

}
