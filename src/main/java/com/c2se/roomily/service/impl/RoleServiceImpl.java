package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Role;
import com.c2se.roomily.repository.RoleRepository;
import com.c2se.roomily.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role getByName(String name) {
        return roleRepository.findByName(name);
    }
}
