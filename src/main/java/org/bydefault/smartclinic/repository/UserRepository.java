package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    Optional<User> findByCode(String code);
}
