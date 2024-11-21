package ui.Admin;

import Main.validation;
import Main.config;
import Crud.userCRUD;
import Authentication.Register;

import java.io.IOException;

public class users extends config {
    
    validation validate = new validation();
    Register reg = new Register();
    userCRUD u = new userCRUD();
    
    boolean isSelected = false;
    
    public void userInterface() throws IOException{
        do{
            System.out.println("================================================================================================================================================================");
            System.out.println("List of users: ");
            u.viewUser();
        
            System.out.print("1. Register a user"
                    + "\n2. Edit user"
                    + "\n3. Delete user"
                    + "\n4. View full info"
                    + "\n5. Back"
                    + "\n0. Exit"
                    + "\nEnter selection: ");
            int userSelect = validate.validateInt();

            switch(userSelect){
                case 1:
                    reg.registerCredentials();
                    break;
                case 2:
                    u.editUser();
                    break;
                case 3:
                    u.deleteUser();
                    break;
                case 4:
                    System.out.print("\nEnter ID: ");
                    int uid = validate.validateInt();
                    
                    while(getSingleValue("SELECT user_id FROM user WHERE user_id = ?", uid) == 0){
                        System.out.print("Error: ID doesn't exist, try again: ");
                        uid = validate.validateInt();
                    }
                    
                    u.viewUserInfo(uid);
                    break;
                case 5:
                    isSelected = true;
                    break;
                case 0:
                    exitMessage();
                    break;
                default:
                    System.out.println("Error: Invalid selection");
            }
        } while(!isSelected);
    }
        
}
