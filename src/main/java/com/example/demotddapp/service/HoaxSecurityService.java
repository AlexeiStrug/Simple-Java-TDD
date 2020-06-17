package com.example.demotddapp.service;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.HoaxRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoaxSecurityService {

    HoaxRepository hoaxRepository;

    public HoaxSecurityService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public boolean isAllowedToDelete(long hoaxId, User loggedInUser) {
        Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
        if (optionalHoax.isPresent()) {
            Hoax inDb = optionalHoax.get();
            return inDb.getUser().getId() == loggedInUser.getId();
        }
        return false;
    }
}
