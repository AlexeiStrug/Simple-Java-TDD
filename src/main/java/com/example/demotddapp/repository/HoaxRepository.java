package com.example.demotddapp.repository;

import com.example.demotddapp.model.Hoax;
import com.example.demotddapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    Page<Hoax> findByUser(User user, Pageable pageable);

    Page<Hoax> findByUserUsername(String username, Pageable pageable);
}
