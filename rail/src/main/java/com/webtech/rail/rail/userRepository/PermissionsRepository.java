package com.webtech.rail.rail.userRepository;


import com.webtech.rail.rail.model.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    Optional<Permissions> findByName(String name);
    boolean existsByName(String name);
}