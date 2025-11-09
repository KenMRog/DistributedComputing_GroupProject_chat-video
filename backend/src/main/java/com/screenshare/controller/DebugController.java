package com.screenshare.controller;

import com.screenshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private Environment env;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/env")
    public ResponseEntity<Map<String, Object>> envInfo() {
        Map<String, Object> info = new HashMap<>();
        String[] profiles = env.getActiveProfiles();
        info.put("activeProfiles", profiles.length == 0 ? new String[]{env.getProperty("spring.profiles.active")} : profiles);

        // datasource url (mask password if present)
        String url = env.getProperty("spring.datasource.url");
        if (url == null) url = env.getProperty("DB_URL");
        info.put("datasourceUrl", url != null ? url : "(not-set)");

        return ResponseEntity.ok(info);
    }

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Object>> userCount() {
        Map<String, Object> res = new HashMap<>();
        long count = userRepository.count();
        res.put("userCount", count);
        return ResponseEntity.ok(res);
    }
}
