package org.bydefault.smartclinic.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.bydefault.smartclinic.dtos.admin.UserDto;
import org.bydefault.smartclinic.entities.Role;
import org.bydefault.smartclinic.services.admin.AdminServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
public class AdminController {

    private final AdminServices services;


    @GetMapping("users/")
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam(required = false) Role role,
                                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                                                    @RequestParam(defaultValue = "") List<String> sortList,
                                                    @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        return ResponseEntity.ok(services.getAllUsers(role, page, size, sortList, sortOrder.toString()));

    }
}
