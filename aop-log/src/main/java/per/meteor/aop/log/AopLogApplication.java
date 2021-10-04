package per.meteor.aop.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author meteor
 * @date 2021-10-04 0:00
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AopLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(AopLogApplication.class , args);
    }
}
