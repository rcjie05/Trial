package Authentication;

import Main.config;
import Main.validation;

import java.io.IOException;
import java.util.*;
import users.*;
import java.sql.*;

public class Login extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    Admin admin = new Admin();
    Member member = new Member();
    Project_Manager manager = new Project_Manager();
    
    int id;
    String user, pass, fname;
    String locatedUser, locatedPass, locatedRole;
    
    public void loginCredentials() throws IOException{
        
            System.out.println("Login:");
            System.out.print("Enter username: ");
            user = sc.nextLine();
            
            while(validate.spaceValidate(user)){
                user = sc.nextLine();
            }

            System.out.print("Enter password: ");
            pass = sc.nextLine();
            
            while(validate.spaceValidate(pass)){
                pass = sc.nextLine();
            }
            
            locateUser(user, pass);
    }
    
    public void locateUser(String username, String password) throws IOException{
        boolean userLocated = false;
        
        try{
            PreparedStatement state = connectDB().prepareStatement("SELECT user_id, first_name, username, password, role FROM user");
            
            try (ResultSet result = state.executeQuery()) {
                while(result.next()){
                    locatedUser = result.getString("username");
                    locatedPass = result.getString("password");
                    locatedRole = result.getString("role");
                    fname = result.getString("first_name");
                    id= result.getInt("user_id");
                    
                    if(username.equals(locatedUser) && password.equals(locatedPass)){
                        userLocated = true;
                        break;
                    }
                }
                result.close();
                
                if(userLocated){
                    switch (locatedRole) {
                        case "admin":
                            admin.displayInterface(id, locatedRole, fname);
                            break;
                        case "member":
                            member.displayInterface(id, locatedRole, fname);
                            break;
                        case "project manager":
                            manager.displayInterface(id, locatedRole, fname);
                            break;
                        default: System.out.println("Error: Role not found.");
                            break;
                    }
                }
                else{
                    System.out.println("\nUser not found.");
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}
