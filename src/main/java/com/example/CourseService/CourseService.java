package com.example.CourseService;


//... imports ...
import com.example.CourseService.Course;
import com.example.CourseService.Course.*; // Exception
import com.example.CourseService.CourseRepository;
import com.example.CourseService.UserServiceClient; // Import Feign Client
import com.example.CourseService.UserDTO; // Import DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections; // Pour Collections.emptyList() / emptySet()


@Service
public class CourseService {

 @Autowired
 private CourseRepository courseRepository; // Renommé pour clarté

 @Autowired(required = false) // Mettre à false car user-service peut ne pas être démarré
 private UserServiceClient userServiceClient;
 
 public Course findByID(int courseId) {
     return courseRepository.findById(courseId)
             .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));
 }

 // Récupère les IDs des utilisateurs inscrits
 public Set<Integer> getEnrolledUserIds(int courseId) {
      Course course = findByID(courseId);
      // Si Option A (Set<Integer> enrolledUserIds dans Course) est utilisée:
      return course.getEnrolledUserIds();
      // Si les inscriptions sont gérées autrement (ex: autre table/service), adapter la logique
      // return Collections.emptySet(); // Placeholder
 }


 // Récupère les DTOs des utilisateurs inscrits en appelant user-service
 public Set<UserDTO> getEnrolledUsers(int courseId) {
     Set<Integer> userIds = getEnrolledUserIds(courseId);

     if (userIds.isEmpty() || userServiceClient == null) {
         return Collections.emptySet();
     }

     try {
         // NOTE: Appeler getUserById pour chaque ID peut être inefficace (N+1 appels réseau).
         // Idéalement, UserServiceClient aurait une méthode comme getUsersByIds(List<Integer> ids).
         // Pour cet exemple, nous faisons des appels multiples :
         return userIds.stream()
                 .map(userId -> {
                     try {
                         return userServiceClient.getUserById(userId);
                     } catch (Exception e) {
                         System.err.println("Failed to fetch user details for ID " + userId + ": " + e.getMessage());
                         return null; // Retourne null si l'appel échoue pour cet ID
                     }
                 })
                 .filter(Objects::nonNull) // Filtre les résultats nulls
                 .collect(Collectors.toSet());
     } catch (Exception e) {
          System.err.println("Error fetching enrolled users details from User Service: " + e.getMessage());
          return Collections.emptySet();
     }
 }


 public List<Course> getAllCourses() {
     return courseRepository.findAll(); // Utilise la méthode standard
 }

 @Transactional
 public void deleteCourseById(int id) {
      // Vérifier si le cours existe avant de supprimer
      if (!courseRepository.existsById(id)) {
          throw new CourseNotFoundException("Cannot delete. Course not found with id: " + id);
      }
      // Optionnel : Gérer la désinscription des utilisateurs si nécessaire avant suppression
     courseRepository.deleteCourseById(id);
 }

 // Récupère les cours créés par un utilisateur spécifique
 public List<Course> getCoursesCreatedByOwner(int ownerUserId) {
     // Vérifier si l'utilisateur existe via UserServiceClient serait une bonne pratique
      // try {
      //     UserDTO user = userServiceClient.getUserById(ownerUserId);
      //     if (user == null) { throw new RuntimeException("Owner user not found"); }
      // } catch (Exception e) { throw new RuntimeException("Cannot verify owner user"); }

     return courseRepository.findByOwnerUserId(ownerUserId);
 }

  // Récupère les cours auxquels un user est inscrit (si Option A dans Course.java)
  public List<Course> getCoursesUserEnrolledIn(Integer userId) {
      return courseRepository.findCoursesEnrolledByUserId(userId);
  }


 // Ajoute un nouveau cours, en associant l'ID du créateur
 @Transactional
 public Course addCourse(Course courseData, int ownerUserId) {
      // Vérifier si l'utilisateur propriétaire existe ? (Appel Feign optionnel)
     // try {
     //     if (userServiceClient != null && userServiceClient.getUserById(ownerUserId) == null) {
     //          throw new RuntimeException("Owner user with id " + ownerUserId + " not found.");
     //     }
     // } catch (Exception e) {
     //      System.err.println("Could not verify owner user " + ownerUserId + ": " + e.getMessage());
          // Décider si on continue ou on lance une erreur
          // throw new RuntimeException("Could not verify owner user.");
     // }

     // Crée un nouvel objet Course pour éviter les problèmes d'état détaché
     Course newCourse = new Course();
     newCourse.setTitle(courseData.getTitle());
     newCourse.setDescription(courseData.getDescription());
     newCourse.setCategorie(courseData.getCategorie());
     newCourse.setPrice(courseData.getPrice());
     newCourse.setCourseUrl(courseData.getCourseUrl());
     newCourse.setOwnerUserId(ownerUserId); // Définit le propriétaire

     return courseRepository.save(newCourse);
 }

 @Transactional
 public Course updateCourse(int courseId, Course updatedCourseData) {
     Course course = findByID(courseId); // Trouve le cours existant

     // Met à jour les champs fournis
     if (updatedCourseData.getTitle() != null) course.setTitle(updatedCourseData.getTitle());
     if (updatedCourseData.getDescription() != null) course.setDescription(updatedCourseData.getDescription());
     if (updatedCourseData.getPrice() != null) course.setPrice(updatedCourseData.getPrice());
     if (updatedCourseData.getCourseUrl() != null) course.setCourseUrl(updatedCourseData.getCourseUrl());
     if (updatedCourseData.getCategorie() != null) course.setCategorie(updatedCourseData.getCategorie());
     // Ne pas permettre de changer le ownerUserId ici ?

     return courseRepository.save(course);
 }

 // Récupère le DTO du propriétaire du cours
 public UserDTO getCourseOwner(int courseId) {
     Course course = findByID(courseId);
     Integer ownerId = course.getOwnerUserId();

     if (ownerId == null || userServiceClient == null) {
         return null; // Ou lancer une exception / retourner un DTO par défaut
     }

     try {
         return userServiceClient.getUserById(ownerId);
     } catch (Exception e) {
          System.err.println("Failed to fetch owner user details for ID " + ownerId + ": " + e.getMessage());
         return null; // Ou lancer une exception
     }
 }

  // Inscrit un utilisateur à un cours (si Option A dans Course.java)
  @Transactional
  public void enrollUser(int courseId, int userId) {
      Course course = findByID(courseId);
      // Optionnel : Vérifier si l'utilisateur existe via Feign
      // try { if(userServiceClient != null && userServiceClient.getUserById(userId) == null) throw ... } catch ...

      course.getEnrolledUserIds().add(userId);
      courseRepository.save(course);
  }

  // Désinscrit un utilisateur d'un cours (si Option A dans Course.java)
  @Transactional
  public void unenrollUser(int courseId, int userId) {
      Course course = findByID(courseId);
      course.getEnrolledUserIds().remove(userId);
      courseRepository.save(course);
  }

}