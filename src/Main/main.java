package Main;

import Authentication.Login;
import Authentication.Register;
import Authentication.Change_Password;

import java.io.IOException;

public class main {
    
    public static void main(String[] args) throws IOException {
        
        validation validate = new validation();
        Change_Password change = new Change_Password();
        Register register = new Register();
        Login login = new Login();
        config conf = new config();
        
        boolean isSelected = false;
        
        do{
            System.out.print("================================================================================================================================================================\n"
                    + "╒=========================╕\n"
                    + "│Project Management System│\n"
                    + "├-------------------------┤\n"
                    + "│[1]| Login               │\n"
                    + "│[2]| Register            │\n"
                    + "│[3]| Reset Password      │\n"
                    + "│[0]| Exit                │\n"
                    + "└-------------------------┘\n"
                    + "\n|Enter choice: ");
            int choice = validate.validateInt();
            
            System.out.println("================================================================================================================================================================");
            switch(choice){
                case 1:
                    login.loginCredentials();
                    break;
                case 2:
                    System.out.println("Register: ");
                    register.registerCredentials();
                    break;
                case 3:
                    System.out.println("Change Password: ");
                    change.changeCredentials();
                    break;
                case 0:
                    conf.exitMessage();
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        } while(!isSelected);
    }
}
