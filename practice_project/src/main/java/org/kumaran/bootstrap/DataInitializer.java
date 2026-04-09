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
        createDefaultUser("employee", "employee123", "employee", "Ronaldo", "Player");
        createDefaultUser("aakash.s@leavepal.com", "Aakash!1234", "employee", "Aakash", "Saravanakumar");
    }

    private void createDefaultUser(String username, String password, String role, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailId(username.contains("@") ? username : username + "@leavepal.com");

        if ("employee".equalsIgnoreCase(role)) {
            long currentEmployeeCount = userRepository.findAll().stream()
                    .filter(existing -> "employee".equalsIgnoreCase(existing.getRole()))
                    .count();
            user.setEmployeeId(String.format("LP-%03d", currentEmployeeCount + 1));
            user.setDepartment("Banking");
            user.setDesignation("Intern");
            user.setReporting("Ramesh Kumar");
            user.setLocation("Chennai");
            user.setJoining("01-Jan-2024");
            user.setPhoneNumber("+91 98765 43210");
            user.setNationality("Indian");
            user.setBloodGroup("O +ve");
            user.setMaritalStatus("Unmarried");
            user.setDob("15-May-2001");
            user.setPersonalEmail(username.contains("@") ? username.replace("@", ".personal@") : username + ".personal@leavepal.com");
            user.setGender("Male");
            user.setAddress("No. 12, Golden Street, Adyar, Chennai - 600020");
        }

        userRepository.save(user);
    }
}
