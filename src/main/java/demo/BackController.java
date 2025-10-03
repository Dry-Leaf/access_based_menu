package demo;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class BackController {
    private final UserService userService;

    public BackController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/find_all")
    public String findAll(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
    
        if (cookies != null) {
          for (Cookie c : cookies) {
              if ("jwt".equals(c.getName())) {
                  token = c.getValue();
                  break;
              }
          }
        }

        if (token != null) {
            Claims claims = JwtUtil.validateToken(token).getBody();

            int user_type = (int) claims.get("user_type");

            if (user_type != 2) {
                return "Unauthorized";
            }
        } else {
            return "Unauthorized";
        }

        var response = userService.findAll().toString();
        return response;
    }

    @PostMapping("/create_account")
    public String create_account(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password, @RequestParam(value = "user_type") String user_type) {
        var response = userService.createUser(username, password, user_type);
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam(value = "username") String username, 
                                   @RequestParam(value = "password") String password,
                                   HttpServletResponse response) {
        var result = userService.findUser(username, password);
        if (result == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        
        String token = JwtUtil.generateToken(username, result);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600); // 1 hour
        response.addCookie(jwtCookie);

        return ResponseEntity.status(HttpStatus.FOUND)
                     .header("Location", "/")
                     .build();
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    Cookie deleteCookie = new Cookie("jwt", null);
                    deleteCookie.setPath("/");      // must match the original path
                    deleteCookie.setHttpOnly(true); // optional, match original
                    deleteCookie.setMaxAge(0);      // tells browser to delete it
                    response.addCookie(deleteCookie);
                    break;
                }
            }
        }        
        
        try {
            response.sendRedirect("/"); // or /app if your home page is /app
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
