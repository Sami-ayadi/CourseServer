package com.example.CourseService;

//... imports ...
import jakarta.persistence.*; // JPA annotations
import java.util.HashSet;
import java.util.Set;
//Pas besoin de JsonIgnore ici si on ne stocke pas l'objet User complet

@Entity
@Table(name="course") // Assurez-vous que cette table existe dans la BDD de course-service
public class Course {
 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private int id_c;
 private String title ;
 private String description ;
 private String price;
 private String courseUrl;

 @Enumerated(EnumType.STRING)
 @Column(name = "categorie")
 private CategorieCourse categorie;

 // Remplace la relation ManyToOne par un simple ID
 @Column(name = "owner_user_id") // Nom de la colonne en BDD
 private Integer ownerUserId;

 // Option A: Gérer les IDs des inscrits directement dans l'entité Course
  @ElementCollection(fetch = FetchType.LAZY) // Charge les IDs seulement si nécessaire
  @CollectionTable(name = "course_enrollments", joinColumns = @JoinColumn(name = "course_id")) // Table séparée pour les inscriptions
  @Column(name = "user_id") // Nom de la colonne contenant l'ID de l'utilisateur inscrit
  private Set<Integer> enrolledUserIds = new HashSet<>();


 // Adaptez le constructeur
 public Course(String title, String description, CategorieCourse categorie, String price, String courseUrl, Integer ownerUserId) {
     this.title = title;
     this.description = description;
     this.categorie = categorie;
     this.price = price;
     this.courseUrl = courseUrl;
     this.ownerUserId = ownerUserId;
 }

 public Course() {}

 // Getters et Setters pour les champs existants et modifiés/ajoutés
 public int getId_c() { return id_c; }
 public void setId_c(int id_c) { this.id_c = id_c; }
 public String getTitle() { return title; }
 public void setTitle(String title) { this.title = title; }
 public String getDescription() { return description; }
 public void setDescription(String description) { this.description = description; }
 public String getPrice() { return price; }
 public void setPrice(String price) { this.price = price; }
 public String getCourseUrl() { return courseUrl; }
 public void setCourseUrl(String courseUrl) { this.courseUrl = courseUrl; }
 public CategorieCourse getCategorie() { return categorie; }
 public void setCategorie(CategorieCourse categorie) { this.categorie = categorie; }
 public Integer getOwnerUserId() { return ownerUserId; }
 public void setOwnerUserId(Integer ownerUserId) { this.ownerUserId = ownerUserId; }

 // Getters/Setters pour enrolledUserIds si Option A est choisie
  public Set<Integer> getEnrolledUserIds() { return enrolledUserIds; }
  public void setEnrolledUserIds(Set<Integer> enrolledUserIds) { this.enrolledUserIds = enrolledUserIds; }

 // Exception interne
 @SuppressWarnings("serial")
 public static class CourseNotFoundException extends RuntimeException {
     public CourseNotFoundException(String message) { super(message); }
 }
}