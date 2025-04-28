package com.example.CourseService;

//DTO minimal pour représenter un utilisateur
public class UserDTO {
 private int id;
 private String nom;
 private String prenom;
 private String email;
 // Ajoutez d'autres champs si nécessaire

 public UserDTO() {}

 // Getters et Setters
 public int getId() { return id; }
 public void setId(int id) { this.id = id; }
 public String getNom() { return nom; }
 public void setNom(String nom) { this.nom = nom; }
 public String getPrenom() { return prenom; }
 public void setPrenom(String prenom) { this.prenom = prenom; }
 public String getEmail() { return email; }
 public void setEmail(String email) { this.email = email; }
}