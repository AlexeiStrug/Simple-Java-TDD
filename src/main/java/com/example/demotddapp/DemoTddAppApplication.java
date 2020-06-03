package com.example.demotddapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoTddAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoTddAppApplication.class, args);
    }

//    @Bean
//    @Profile("!test")
//    CommandLineRunner run(UserService userService) {
//        return new CommandLineRunner() {
//            @Override
//            public void run(String... args) throws Exception {
//                IntStream.rangeClosed(1, 15)
//                        .mapToObj(i -> {
//                            User user = new User();
//                            user.setUsername("user" + i);
//                            user.setDisplayName("displayName" + i);
//                            user.setPassword("P5ssword");
//                            return user;
//                        })
//                        .forEach(userService::save);
//            }
//        };
//    }

}
