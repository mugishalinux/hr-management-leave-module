package com.leave.management.system.util;



import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.model.Permissions;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("Running init");
        insertUsers();
        System.out.println("Done init");
    }

    private void insertUsers() {
        String adminEmail = "admin@gmail.com";
        if (userRepository.findByEmail(adminEmail) == null) {
            User user = new User();
            user.setEmail(adminEmail);
            user.setPassword(passwordEncoder.encode("admin"));
            user.setFullName("Admin");
            user.setPermissions(UserPermission.ADMIN);
            user.setAccountEnabled(true);
            userRepository.save(user);
            System.out.println("✅ Admin user seeded");
        } else {
            System.out.println("ℹ️ Admin user already exists, skipping seeding");
        }
    }
}
