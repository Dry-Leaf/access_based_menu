package demo;

import org.springframework.web.bind.annotation.GetMapping;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class FrontController {
  @GetMapping("/")
  public String hello(HttpServletRequest request, Model model) {
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

      String username = claims.getSubject();
      int user_type = (int) claims.get("user_type");

      model.addAttribute("username", username);
      model.addAttribute("user_type", user_type);
    }

    return "app";
  }
}
