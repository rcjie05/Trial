package Authentication;

import Main.config;
import Main.validation;

import java.util.Scanner;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Change_Password extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    
    boolean isFound = false;
    
    public void changeCredentials(){
        do{
            System.out.print("\nEnter email: ");
            String email = sc.nextLine();
            
            while(validate.spaceValidate(email)){
                email = sc.nextLine();
            }
                
            if(!findEmail(email)){
                System.out.println("Error: Email not found.");
                break;
            }
            
        } while(!isFound);
        
    }
    
    private boolean findEmail(String getEmail){
        try{
            PreparedStatement findEmail = connectDB().prepareStatement("SELECT email, username, password FROM user WHERE email = ?");
            findEmail.setString(1, getEmail);
            
            try(ResultSet checkEmail = findEmail.executeQuery()){
                if(getEmail.equals(checkEmail.getString("email"))){
                    
                    System.out.println("--------------------------------------------------------------------------------");
                    System.out.println("Username found: "+checkEmail.getString("username"));
                    
                    String getPassword = checkEmail.getString("password");
                    checkEmail.close();
                    
                    setPassword(getEmail, getPassword);
                    isFound = true;
                    return true;
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
    
    private void setPassword(String userEmail, String userPassword){
        
        System.out.print("\nEnter new password: ");
        String newPassword = sc.nextLine();
        
        while(validate.spaceValidate(newPassword)){
            newPassword = sc.nextLine();
        }
        
        while(newPassword.equals(userPassword)){
            System.out.print("Error: Must not be an old password, try again: ");
            newPassword = sc.nextLine();
            
            while(validate.spaceValidate(newPassword)){
                newPassword = sc.nextLine();
            }
        }

        System.out.print("Confirm password: ");
        String confirmPass = sc.nextLine();
        
        while(validate.spaceValidate(confirmPass)){
            confirmPass = sc.nextLine();
        }

        if(!confirmPass.equals(newPassword)){
            System.out.println("Passwords don't match.");
        }
        else{
            System.out.print("Confirm credentials? [y/n]: ");
            String confirm = sc.nextLine();

            if(validate.confirm(confirm)){
                String updatePass = "UPDATE user SET password = ? WHERE email = ?";
                updateRecord(updatePass, confirmPass, userEmail);
            } else{
                System.out.println("Change password cancelled.");
            }
        }
    }
}
