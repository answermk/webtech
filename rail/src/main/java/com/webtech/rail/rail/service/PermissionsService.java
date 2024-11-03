package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.Permissions;
import com.webtech.rail.rail.userRepository.PermissionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionsService {

    private final PermissionsRepository permissionsRepository;

    @Autowired
    public PermissionsService(PermissionsRepository permissionsRepository) {
        this.permissionsRepository = permissionsRepository;
    }

    @Transactional(readOnly = true)
    public List<Permissions> getAllPermissions() {
        return permissionsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Permissions> getPermissionById(Long id) {
        return permissionsRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Permissions> getPermissionByName(String name) {
        return permissionsRepository.findByName(name);
    }

    @Transactional
    public Permissions createPermission(Permissions permission) {
        if (permissionsRepository.existsByName(permission.getName())) {
            throw new RuntimeException("Permission with name " + permission.getName() + " already exists");
        }
        return permissionsRepository.save(permission);
    }

    @Transactional
    public Permissions updatePermission(Long id, Permissions permission) {
        if (!permissionsRepository.existsById(id)) {
            throw new RuntimeException("Permission not found with id: " + id);
        }
        permission.setPermissionId(id);
        return permissionsRepository.save(permission);
    }

    @Transactional
    public void deletePermission(Long id) {
        if (!permissionsRepository.existsById(id)) {
            throw new RuntimeException("Permission not found with id: " + id);
        }
        permissionsRepository.deleteById(id);
    }
}
