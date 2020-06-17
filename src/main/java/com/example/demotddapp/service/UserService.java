package com.example.demotddapp.service;

import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.UserUpdateVM;
import com.example.demotddapp.repository.UserRepository;
import com.example.demotddapp.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    FileService fileService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FileService fileService) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public User save(User user) {
//        User find = userRepository.findByUsername(user.getUsername());
//        if (find != null) {
//            throw new DuplicateUsernameException();
//        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User update(Long id, UserUpdateVM userUpdateVM) {
        User userInDb = userRepository.getOne(id);
        if (userInDb != null) {
            userInDb.setDisplayName(userUpdateVM.getDisplayName());
            if (userUpdateVM.getImage() != null) {
                String savedImageName = null;
                try {
                    savedImageName = fileService.saveProfileImage(userUpdateVM.getImage());
                    fileService.deleteProfileImage(userInDb.getImage());
                    userInDb.setImage(savedImageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return userRepository.save(userInDb);
        }
        return null;
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
