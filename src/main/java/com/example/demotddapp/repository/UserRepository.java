package com.example.demotddapp.repository;

import com.example.demotddapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Page<User> findByUsernameNot(String username, Pageable pageable);

//    For UserProjection way
//    @Query(value = "SELECT * FROM User", nativeQuery = true)
//    @Query(value = "SELECT u FROM User u")
//    Page<UserProjection> getAllUsersProjection(Pageable pageable);


}
