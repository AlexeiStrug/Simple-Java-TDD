package com.example.demotddapp.service;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import com.example.demotddapp.repository.HoaxRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;
    UserService userService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
    }

    public Hoax save(Hoax hoax, User user) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDB = userService.getUserByUsername(username);
        return hoaxRepository.findByUser(inDB, pageable);
    }
//    version 1 with spec
//    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
//        Specification<Hoax> hoaxSpecification = Specification.where(idLessThan(id));
//        if (username == null) {
////            return hoaxRepository.findByIdLessThan(id, pageable);
//            return hoaxRepository.findAll(hoaxSpecification, pageable);
//        } else {
//            User inDB = userService.getUserByUsername(username);
////            return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
//            return hoaxRepository.findAll(hoaxSpecification.and(userIs(inDB)), pageable);
//        }
//    }

    //    version 2 with spec
    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> hoaxSpecification = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            hoaxSpecification = hoaxSpecification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(hoaxSpecification, pageable);
    }

//    public Page<Hoax> getOldHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDB = userService.getUserByUsername(username);
//        return hoaxRepository.findByIdLessThanAndUser(id, inDB, pageable);
//    }

//      version 1 without spec
//    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
//        if (username == null) {
//            return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
//        } else {
//            User inDB = userService.getUserByUsername(username);
//            return hoaxRepository.findByIdGreaterThanAndUser(id, inDB, pageable.getSort());
//        }
//    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> hoaxSpecification = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            hoaxSpecification = hoaxSpecification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(hoaxSpecification, pageable.getSort());
    }

//    public List<Hoax> getNewHoaxesOfUser(long id, String username, Pageable pageable) {
//        User inDB = userService.getUserByUsername(username);
//        return hoaxRepository.findByIdGreaterThanAndUser(id, inDB, pageable.getSort());
//    }

//    version 1 without spec
//    public long getNewHoaxCount(long id, String username) {
//        if (username == null) {
//            return hoaxRepository.countByIdGreaterThan(id);
//        } else {
//            User inDB = userService.getUserByUsername(username);
//            return hoaxRepository.countByIdGreaterThanAndUser(id, inDB);
//        }
//    }

    public long getNewHoaxCount(long id, String username) {
        Specification<Hoax> hoaxSpecification = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            hoaxSpecification = hoaxSpecification.and(userIs(inDB));
        }
        return hoaxRepository.count(hoaxSpecification);
    }

//    public long getNewHoaxCountOfUser(long id, String username) {
//        User inDB = userService.getUserByUsername(username);
//        return hoaxRepository.countByIdGreaterThanAndUser(id, inDB);
//    }

    private Specification<Hoax> userIs(User user) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    private Specification<Hoax> idLessThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("id"), id);
        };
    }

    private Specification<Hoax> idGreaterThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("id"), id);
        };
    }

}
