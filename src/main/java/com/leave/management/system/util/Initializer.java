package com.leave.management.system.util;



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
        if (userRepository.findByUsername("admin") == null){
            User user = new User("admin", passwordEncoder.encode("Da2SVDkq!^fUBo8zkdcYlf95j6rltBwHRAqogfHVsKHfUEBrhZ"));
            user.addAuthority(Permissions.VIEW_DASHBOARD.toString());
            user.addAuthority(Permissions.USER_MANAGEMENT.toString());
            user.addAuthority(Permissions.POST_MANAGEMENT.toString());
            userRepository.save(user);
        }
        if (userRepository.findByUsername("user") == null){
            User user = new User("user", passwordEncoder.encode("SVDkq!^fUBo8zkdcYlf95j6rltBwHRAqogfHVsKHfUEBrhZ"));
            user.addAuthority(Permissions.VIEW_DASHBOARD.toString());
            user.addAuthority(Permissions.POST_MANAGEMENT.toString());
            userRepository.save(user);
        }
    }
}
