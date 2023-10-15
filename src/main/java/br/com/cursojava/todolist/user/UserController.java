package br.com.cursojava.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody UserModel userReceived) {

        // Checking if user already exists
        var userExists = this.userRepository.findByUsername(userReceived.getUsername());
        if (userExists != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }

        // Hashing password
        var passwordHashred = BCrypt.withDefaults().hashToString(12, userReceived.getPassword().toCharArray());
        userReceived.setPassword(passwordHashred);

        // Saving user
        var userCreated = this.userRepository.save(userReceived);
        return ResponseEntity.status(HttpStatus.OK).body(userCreated);
    }
}
