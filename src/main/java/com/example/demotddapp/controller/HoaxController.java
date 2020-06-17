package com.example.demotddapp.controller;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.HoaxVm;
import com.example.demotddapp.service.HoaxService;
import com.example.demotddapp.utils.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    HoaxVm createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        return new HoaxVm(hoaxService.save(hoax, user));
    }

    @GetMapping("/hoaxes")
    Page<HoaxVm> getAllHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable).map(HoaxVm::new);
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVm> getHoaxesOfUser(@PathVariable String username, Pageable pageable) {
        return hoaxService.getHoaxesOfUser(username, pageable).map(HoaxVm::new);
    }
}

