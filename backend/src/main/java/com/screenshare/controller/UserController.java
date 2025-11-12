package com.screenshare.controller;

import com.screenshare.dto.UserDto;
import com.screenshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.screenshare.repository.ChatRoomRepository chatRoomRepository;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(@org.springframework.web.bind.annotation.RequestParam(required = false) String q,
                                                     @org.springframework.web.bind.annotation.RequestParam(required = false) Long excludeActiveDmWith,
                                                     @org.springframework.web.bind.annotation.RequestParam(required = false) Long excludeMemberOfRoom) {
        try {
            List<UserDto> users;
            if (q != null && !q.trim().isEmpty()) {
                String term = q.trim();
                users = userRepository.findTop5ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, term)
                        .stream()
                        .filter(u -> {
                            if (excludeActiveDmWith != null) {
                                if (chatRoomRepository.findDirectMessageRoom(excludeActiveDmWith, u.getId()).isPresent()) {
                                    return false;
                                }
                            }
                            if (excludeMemberOfRoom != null) {
                                if (chatRoomRepository.isUserMemberOfRoom(excludeMemberOfRoom, u.getId())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .map(UserDto::new)
                        .collect(Collectors.toList());
            } else {
                users = userRepository.findAll()
                        .stream()
                        .filter(u -> {
                            if (excludeActiveDmWith != null) {
                                if (chatRoomRepository.findDirectMessageRoom(excludeActiveDmWith, u.getId()).isPresent()) {
                                    return false;
                                }
                            }
                            if (excludeMemberOfRoom != null) {
                                if (chatRoomRepository.isUserMemberOfRoom(excludeMemberOfRoom, u.getId())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .map(UserDto::new)
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
