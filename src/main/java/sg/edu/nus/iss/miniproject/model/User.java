package sg.edu.nus.iss.miniproject.model;

import java.util.Random;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// class for creating a new user account
public class User {
    @NotEmpty (message = "Name field cannot be empty")
    @Size(min = 3, max = 50, message = "Name must be within 3 and 50 characters")
    @Pattern(regexp = "[A-Za-z]*$", message="Name field can only contain alphabets")
    private String name;

    @NotEmpty(message = "Email field cannot be empty")
    @Email(message = "Email field is not in the correct format")
    private String email;

    @NotEmpty(message = "Password field cannot be empty")
    @Size(min = 8, message = "Minimum of 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\!\\?~/\\-'\\[\\];\\(\\)\\{\\}:\\<\\>\\.,@#\\$%\\^&+\\=\\*\\_\\\\])(?=\\S+$).*$", 
        message = "At least 1 digit, 1 uppercase, 1 lowercase, 1 special character and no white space")
    private String password;

    @NotEmpty(message = "Field cannot be empty")
    private String confirmPassword;

    private String id;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User(){
        Random rand = new Random();
        int n = rand.nextInt();
        this.id = Integer.toHexString(n);
    }

}
