package org.example.accountservice.payload.security;

import java.util.HashSet;
import java.util.Set;

/**
 * @author b1go
 * @date 6/26/22 5:25 PM
 */
public class SignUpDto {
    private String username;
    private String email;
    private String password;
    private Set<String> roles = new HashSet<>();
    private String shippingAddress;

    public SignUpDto(String username, String account, String email, String password, String shippingAddress) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.shippingAddress = shippingAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRole() {return roles; };

    public void setRole(Set<String> role) {this.roles = role;};
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
