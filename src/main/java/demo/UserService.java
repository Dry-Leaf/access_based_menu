package demo;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> findAll() {
        return repo.findAll();
    }

    public int findUser(String username, String password) {
        return repo.findUser(username, password);
    }

    public String createUser(String username, String password, String user_type) {
        var message = repo.createUser(username, password, user_type);
        return (message);
    }
}
