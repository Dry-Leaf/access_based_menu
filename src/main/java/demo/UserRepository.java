package demo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("SELECT username FROM users");
    }

    //USER VERIFICATION
    public int findUser(String username, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT password_hash, user_type FROM users WHERE username = ? LIMIT 1",
                username
            );

            String passwordHash = (String) row.get("password_hash");
            boolean matches = encoder.matches(password, passwordHash);

            if (!matches) {
                return 0;
            }

            String userType = (String) row.get("user_type");

            if (userType.equals("Admin")) {
                // Admin
                return 2;
            }

            // Normal User
            return 1;
        } catch (EmptyResultDataAccessException e) {
            // User does not exist.
            return 0;
        }
    }

    //USER CREATION
    public String createUser(String username, String password, String user_type) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(password);

        try { 
            jdbcTemplate.update("INSERT INTO users(username, password_hash, user_type) VALUES(?, ?, ?)", username, hashed, user_type);
            return String.format("User: %s created.", username); 
        } catch (Exception  e) {
            return String.format("User: %s already exists", username); 
        }
    }
}