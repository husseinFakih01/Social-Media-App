package net.CSC4111.ourApp.WSUSocial.models;import com.google.firebase.database.IgnoreExtraProperties;
@IgnoreExtraProperties
public class User{public String username;public String email;public User(){}public User(String username, String email){this.username = username;this.email = email;}}