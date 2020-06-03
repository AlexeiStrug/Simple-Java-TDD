package com.example.demotddapp;

import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void test_findByUsername_WhenUserExist_returnUser() {
        testEntityManager.persist(TestUtils.createUser());
        User find = userRepository.findByUsername("test-user");
        assertThat(find).isNotNull();

    }

    @Test
    public void test_findByUsername_WhenUserNotExist_returnNull() {
        User find = userRepository.findByUsername("some-non-exist-user");
        assertThat(find).isNull();

    }

}
