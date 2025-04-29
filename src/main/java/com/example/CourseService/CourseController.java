package com.example.CourseService;


import com.example.CourseService.Course.CourseNotFoundException;
//... imports ...
import com.example.CourseService.Course;
import com.example.CourseService.Course.*; // Exception
import com.example.CourseService.CourseService;
import com.example.CourseService.UserDTO; // Import DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Annotations Web


import java.util.List;
import java.util.Optional;
import java.util.Set;


@RestController
@RequestMapping("/api/courses") // Préfixe pour les routes
@CrossOrigin("*") // À affiner en production
public class CourseController {

 @Autowired
 private CourseService courseService; // Renommé

 // GET /api/courses/{id}
 @GetMapping("/{id}")

 public ResponseEntity<Course> findCourseById(@PathVariable int id) {
     try {
         Course course = courseService.findByID(id);
         return ResponseEntity.ok(course);
     } catch (CourseNotFoundException e) {
         return ResponseEntity.notFound().build();
     }
 }

 // PUT /api/courses/update/{courseId}
 @PutMapping("/update/{courseId}")
 public ResponseEntity<?> updateCourse(@PathVariable int courseId, @RequestBody Course updatedCourseData) {
     try {
         Course updatedCourse = courseService.updateCourse(courseId, updatedCourseData);
         return ResponseEntity.ok(updatedCourse);
     } catch (CourseNotFoundException e) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating course.");
     }
 }

 // POST /api/courses/add/{ownerUserId} -> Prend l'ID du créateur en paramètre URL
 @PostMapping("/add/{ownerUserId}")
 public ResponseEntity<?> addCourse(@RequestBody Course courseData, @PathVariable int ownerUserId) {
      try {
          // Le service gère la logique, y compris la vérification de l'owner si implémentée
          Course newCourse = courseService.addCourse(courseData, ownerUserId);
          return new ResponseEntity<>(newCourse, HttpStatus.CREATED);
      } catch (RuntimeException e) { // Attrape les erreurs potentielles de vérification user
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding course.");
      }
 }


 // GET /api/courses/{courseId}/enrolled-users -> Retourne les DTOs des utilisateurs inscrits
 @GetMapping("/{courseId}/enrolled-users")
 public ResponseEntity<Set<UserDTO>> getEnrolledUsersForCourse(@PathVariable int courseId) {
     try {
         Set<UserDTO> users = courseService.getEnrolledUsers(courseId);
         return ResponseEntity.ok(users);
     } catch (CourseNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Ou Set vide ?
     } catch (Exception e) {
          System.err.println("Error fetching enrolled users for course " + courseId + ": " + e.getMessage());
          // Peut indiquer un problème de communication avec user-service
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Ou Set vide ?
     }
 }

  // GET /api/courses/{courseId}/enrolled-users/ids -> Retourne juste les IDs des utilisateurs inscrits
  @GetMapping("/{courseId}/enrolled-users/ids")
  public ResponseEntity<Set<Integer>> getEnrolledUserIdsForCourse(@PathVariable int courseId) {
      try {
          Set<Integer> userIds = courseService.getEnrolledUserIds(courseId);
          return ResponseEntity.ok(userIds);
      } catch (CourseNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
  }


 // GET /api/courses -> Récupère tous les cours
 @GetMapping
 public List<Course> getAllCourses() {
     return courseService.getAllCourses();
 }

 // DELETE /api/courses/{id}
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteCourseByID(@PathVariable int id) {
      try {
          courseService.deleteCourseById(id);
          return ResponseEntity.noContent().build(); // 204 No Content
      } catch (CourseNotFoundException e) {
          return ResponseEntity.notFound().build(); // 404 Not Found
      } catch (Exception e) {
          System.err.println("Error deleting course " + id + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
 }

 // GET /api/courses/owner/{userId} -> Récupère les cours créés par un utilisateur
 @GetMapping("/owner/{userId}")
 public ResponseEntity<List<Course>> getCoursesCreatedByOwner(@PathVariable int userId) {
      // Le service peut potentiellement vérifier si l'user existe via Feign
      List<Course> courses = courseService.getCoursesCreatedByOwner(userId);
      return ResponseEntity.ok(courses);
      // Gérer les exceptions si la vérification de l'utilisateur échoue dans le service
 }

  // GET /api/courses/user/{userId}/enrolled -> Récupère les cours auxquels un user est inscrit
  @GetMapping("/user/{userId}/enrolled")
  public ResponseEntity<List<Course>> getCoursesUserEnrolledIn(@PathVariable Integer userId) {
      List<Course> courses = courseService.getCoursesUserEnrolledIn(userId);
      return ResponseEntity.ok(courses);
  }

 // GET /api/courses/{courseId}/owner -> Récupère le DTO du propriétaire du cours
 @GetMapping("/{courseId}/owner")
 public ResponseEntity<UserDTO> getCourseOwner(@PathVariable int courseId) {
     try {
         UserDTO owner = courseService.getCourseOwner(courseId);
         if (owner != null) {
             return ResponseEntity.ok(owner);
         } else {
             // Soit le cours n'a pas de propriétaire, soit user-service n'a pas répondu
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Ou 503 si Feign a échoué ?
         }
     } catch (CourseNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
     } catch (Exception e) {
          System.err.println("Error fetching owner for course " + courseId + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
     }
 }

  // POST /api/courses/{courseId}/enroll/{userId} -> Inscrire un utilisateur
  @PostMapping("/{courseId}/enroll/{userId}")
  public ResponseEntity<String> enrollUserInCourse(@PathVariable int courseId, @PathVariable int userId) {
      try {
          courseService.enrollUser(courseId, userId);
          return ResponseEntity.ok("User " + userId + " enrolled in course " + courseId);
      } catch (CourseNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
      } catch (Exception e) { // Peut être une erreur de contrainte BDD ou autre
          System.err.println("Error enrolling user " + userId + " in course " + courseId + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Enrollment failed.");
      }
  }

  // DELETE /api/courses/{courseId}/unenroll/{userId} -> Désinscrire un utilisateur
  @DeleteMapping("/{courseId}/unenroll/{userId}")
  public ResponseEntity<String> unenrollUserFromCourse(@PathVariable int courseId, @PathVariable int userId) {
      try {
          courseService.unenrollUser(courseId, userId);
          return ResponseEntity.ok("User " + userId + " unenrolled from course " + courseId);
      } catch (CourseNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
      } catch (Exception e) {
          System.err.println("Error unenrolling user " + userId + " from course " + courseId + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unenrollment failed.");
      }
  }

}