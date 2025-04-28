package com.example.CourseService;

import com.example.CourseService.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List; // Import List

// Le nom "user-service" DOIT correspondre au spring.application.name du service cible
@FeignClient(name = "UserService")
public interface UserServiceClient {

    // Doit correspondre à un endpoint DANS user-service (ex: /api/users/{id})
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") int userId);

     // Optionnel: pour récupérer plusieurs utilisateurs par leurs IDs
     // Nécessite un endpoint correspondant dans user-service, ex: /api/users/batch?ids=1,2,3
     // @GetMapping("/api/users/batch")
     // List<UserDTO> getUsersByIds(@RequestParam("ids") List<Integer> userIds);

}