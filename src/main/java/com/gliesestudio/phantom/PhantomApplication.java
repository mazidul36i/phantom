package com.gliesestudio.phantom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PhantomApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhantomApplication.class, args);
        System.out.println("🕶️ Phantom is online and ready...");
    }

}
