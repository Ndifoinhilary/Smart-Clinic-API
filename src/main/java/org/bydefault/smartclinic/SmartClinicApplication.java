package org.bydefault.smartclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartClinicApplication.class, args);
    }

}
