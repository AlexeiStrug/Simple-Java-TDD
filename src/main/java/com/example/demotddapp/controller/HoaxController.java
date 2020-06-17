package com.example.demotddapp.controller;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.model.dto.HoaxVm;
import com.example.demotddapp.service.HoaxService;
import com.example.demotddapp.utils.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping({"/hoaxes/{id:[0-9]+}", "/users/{username}/hoaxes/{id:[0-9]+}"})
    ResponseEntity<?> getHoaxesRelative(@PathVariable long id,
                                        Pageable pageable,
                                        @PathVariable(required = false) String username,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction,
                                        @RequestParam(name ="count", defaultValue = "false") boolean count) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, username, pageable).map(HoaxVm::new));
        }
        if (count) {
            long newHoaxCount = hoaxService.getNewHoaxCount(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
        }
        List<HoaxVm> newHoaxes = hoaxService.getNewHoaxes(id, username, pageable)
                .stream()
                .map(HoaxVm::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(newHoaxes);
    }

//    @GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
//    ResponseEntity<?> getHoaxesRelativeOfUser(@PathVariable String username,
//                                              @PathVariable long id, Pageable pageable,
//                                              @RequestParam(name = "direction", defaultValue = "after") String direction,
//                                              @RequestParam(name ="count", defaultValue = "false") boolean count) {
//        if (!direction.equalsIgnoreCase("after")) {
//            return ResponseEntity.ok(hoaxService.getOldHoaxesOfUser(id, username, pageable).map(HoaxVm::new));
//        }
//        if(count) {
//            long newHoaxCountOfUser = hoaxService.getNewHoaxCountOfUser(id, username);
//            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCountOfUser));
//        }
//        List<HoaxVm> newHoaxesOfUser = hoaxService.getNewHoaxesOfUser(id, username, pageable)
//                .stream()
//                .map(HoaxVm::new)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(newHoaxesOfUser);
//    }


}

