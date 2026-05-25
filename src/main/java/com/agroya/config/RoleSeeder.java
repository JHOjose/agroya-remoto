package com.agroya.config;

import com.agroya.model.Role;
import com.agroya.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Creamos los roles requeridos si no existen en la Base de Datos
        createRoleIfNotFound("ROLE_COMPRADOR");
        createRoleIfNotFound("ROLE_PRODUCTOR");
        createRoleIfNotFound("ROLE_ADMIN");

        System.out.println(" Roles inicializados correctamente en la base de datos.");
    }

    private void createRoleIfNotFound(String name) {
        Optional<Role> roleOpt = roleRepository.findByName(name);
        if (roleOpt.isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}