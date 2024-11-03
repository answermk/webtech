package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.model.Permissions;
import com.webtech.rail.rail.service.PermissionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionsController {

    private final PermissionsService permissionsService;

    @Autowired
    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @GetMapping
    public ResponseEntity<List<Permissions>> getAllPermissions() {
        return ResponseEntity.ok(permissionsService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permissions> getPermissionById(@PathVariable Long id) {
        return permissionsService.getPermissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Permissions> createPermission(@RequestBody Permissions permission) {
        try {
            Permissions createdPermission = permissionsService.createPermission(permission);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permissions> updatePermission(@PathVariable Long id, @RequestBody Permissions permission) {
        try {
            Permissions updatedPermission = permissionsService.updatePermission(id, permission);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        try {
            permissionsService.deletePermission(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
