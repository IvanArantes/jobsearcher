package br.com.iarts.jobsearch.users.controller;

import br.com.iarts.jobsearch.users.entity.User;
import br.com.iarts.jobsearch.users.entity.AuthResponse;
import br.com.iarts.jobsearch.users.exception.AuthenticationException;
import br.com.iarts.jobsearch.users.service.UserService;
import br.com.iarts.jobsearch.users.service.UserServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static br.com.iarts.jobsearch.security.SecurityConstants.*;

@RestController
@RequestMapping("/auth")
public class ApplicationUserController {
    private UserService userService;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public ApplicationUserController(UserServiceImpl userService,
                                     BCryptPasswordEncoder encoder) {
        this.userService = userService;
        this.passwordEncoder = encoder;
    }

    @PostMapping("/sign-up")
    public String signUp(@RequestBody User user) {
       return userService.signUpUser(user);
    }

    @PostMapping("/login")
    public AuthResponse logIn(@RequestBody User user, HttpServletResponse resp) {
        User userFromDB = this.userService.findUserByName(user.getUsername());
        String token = "";
        if(userFromDB!=null){
           if(passwordEncoder.matches(user.getPassword(),(userFromDB.getPassword()))) {
                token = Jwts.builder()
                       .setSubject((user.getUsername()))
                       .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                       .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                       .compact();

           } else {
               resp.setStatus(403);
               throw new AuthenticationException("Invalid authentication");
           }
       }else{
           resp.setStatus(403);
           throw  new AuthenticationException("Invalid authentication");
       }
        return new AuthResponse(token);
    }

    @GetMapping("/info")
    public User getUserInfo(){
        return userService.getLoggedUserInfo();
    }
}
