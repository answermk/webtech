package com.webtech.rail.rail.userRepository;

import com.webtech.rail.rail.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findAll();
    Optional<Route> findById(Long id);
}
