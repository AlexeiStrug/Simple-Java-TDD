package com.example.demotddapp.utils;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;

public class TestUtils {

    public static User createUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P5ssword");
        user.setImage("profile-image.png");
        return user;
    }

    public static User createUser(String username) {
        User user = createUser();
        user.setUsername(username);
        return user;
    }

    public static Hoax createHoax() {
        Hoax hoax = new Hoax();
        hoax.setContent("test content for test hoax");
        return hoax;
    }
}
