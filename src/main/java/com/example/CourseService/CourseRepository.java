package com.example.CourseService;

//... imports ...
import com.example.CourseService.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;


import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

 // findById est déjà fourni par JpaRepository

 // Requête pour trouver les cours par l'ID du propriétaire
 List<Course> findByOwnerUserId(Integer ownerUserId);

 // Garder si getAllCourses a une logique spécifique, sinon JpaRepository.findAll() suffit
 @Query("SELECT c FROM Course c")
 List<Course> getAllCourses();

 @Transactional
 @Modifying
 @Query("delete from Course c where c.id_c = :id")
 void deleteCourseById(@Param("id") int id); // Renommer pour clarté

 // Optionnel : Requête pour trouver les cours auxquels un user est inscrit (si Option A dans Course.java)
 @Query("SELECT c FROM Course c JOIN c.enrolledUserIds uid WHERE uid = :userId")
 List<Course> findCoursesEnrolledByUserId(@Param("userId") Integer userId);
}