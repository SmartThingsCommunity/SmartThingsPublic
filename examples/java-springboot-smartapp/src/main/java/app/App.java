package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String... args) throws Exception {
        SpringApplication.run(App.class, args);
    }
}
