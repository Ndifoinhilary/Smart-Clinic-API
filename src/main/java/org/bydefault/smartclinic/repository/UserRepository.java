package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Role;
import org.bydefault.smartclinic.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    Optional<User> findByCode(String code);

    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role)")
    Page<User> getAllUsers(@Param("role") Role role, Pageable pageable);

    List<User> findByRole(Role role);
}
