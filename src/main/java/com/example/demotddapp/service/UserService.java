package com.example.demotddapp.service;

import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
//        User find = userRepository.findByUsername(user.getUsername());
//        if (find != null) {
//            throw new DuplicateUsernameException();
//        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getUsers(User loggedUser, Pageable pageable) {
        if (loggedUser != null) {
            return userRepository.findByUsernameNot(loggedUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserByUsername(String username) {
        User found = userRepository.findByUsername(username);
        if (found == null) {
            throw new NotFoundException(username + " not found.");
        }
        return found;
    }
}
