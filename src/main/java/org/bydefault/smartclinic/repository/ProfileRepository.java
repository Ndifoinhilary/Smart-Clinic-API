package org.bydefault.smartclinic.repository;

import org.bydefault.smartclinic.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
