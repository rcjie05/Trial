package Authentication;

import Main.config;
import Main.validation;

import java.util.*;

public class Register extends config{
    validation validate = new validation();
    
    String confirmPass, mname;
    
    public void registerCredentials(){
        Scanner sc = new Scanner(System.in);
        System.out.println("================================================================================================================================================================");
        System.out.print("Enter first name: ");
        String fname = sc.nextLine();
        
        System.out.print("Do you have a middle name? [y/n]: ");
        String haveMiddle = sc.nextLine();
        
        if(validate.confirm(haveMiddle)){
            System.out.print("Enter middle name: ");
            mname = sc.nextLine();
        } else{
            mname = "N/A";
        }
        
        System.out.print("Enter last name: ");
        String lname = sc.nextLine();
        
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        
        while(validate.spaceValidate(username)){
            username = sc.nextLine();
        }
        
        while(validate.userValidate(username)){
            username = sc.nextLine();
        }
        
        System.out.print("Enter email address: ");
        String email = sc.nextLine();
        
        while(validate.emailValidate(email)){
            email = sc.nextLine();
        }
        
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        
        while(validate.spaceValidate(password)){
            password = sc.nextLine();
        }
        
        System.out.print("Confirm password: ");
        confirmPass = sc.nextLine();
        
        while(validate.spaceValidate(confirmPass)){
            confirmPass = sc.nextLine();
        }
        
        while(!confirmPass.equals(password)){
            System.out.print("Passwords don't match, try again: ");
            confirmPass = sc.nextLine();
            
            while(validate.spaceValidate(confirmPass)){
                confirmPass = sc.nextLine();
            }
        }
        
        System.out.print("Confirm registration? [y/n]: ");
        String confirm = sc.nextLine();
        
        if(confirm.equals("y")){
            String sql = "INSERT INTO user (first_name, middle_name, last_name, username, email, password, role) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'member')";
            
            addRecord(sql,fname, mname, lname, username, email, confirmPass);
        } else{
            System.out.println("Registration Cancelled.");
        }
    }
}
