package com.securestore.dto;

import javax.validation.constraints.*;
import java.util.Set;

public class SignupRequest {

    @NotBlank(message = "username must not be null")
    @Size(min = 2, max = 12, message = "username size must be between 2 and 12")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "username must not contain special characters")
    private String username;
 
    @NotBlank
    @Size(max = 50)
    private String name;

    private int age;
    
    private Set<String> role;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
  
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<String> getRole() {
      return this.role;
    }
    
    public void setRole(Set<String> role) {
      this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "SignupRequest{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", role=" + role +
                ", password='" + password + '\'' +
                '}';
    }
}