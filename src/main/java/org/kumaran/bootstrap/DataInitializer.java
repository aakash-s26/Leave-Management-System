package org.kumaran.bootstrap;

import org.kumaran.model.UserAccount;
import org.kumaran.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        createDefaultUser("admin", "admin123", "admin", "Admin", "User");
    }

    private void createDefaultUser(String username, String password, String role, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailId(username.contains("@") ? username : username + "@leavepal.com");
        user.setDesignation(role);

        userRepository.save(user);
    }
}
