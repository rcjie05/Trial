package Users;

import Main.config;
import Main.validation;
import ui.Admin.project;
import ui.Admin.task;
import ui.Admin.users;
import Crud.userCRUD;
import ui.Admin.team;
import ui.Admin.team_members;

import java.io.IOException;

public class Admin extends config {
    
    validation validate = new validation();
    team_members tmm = new team_members();
    project proj = new project();
    userCRUD u = new userCRUD();
    users usr = new users();
    task tl = new task();
    team tm = new team();
    
    boolean isSelected = false;
            
    public void displayInterface(int uid, String user_role, String first_name) throws IOException {
        String[] name = first_name.split(" ");
        System.out.printf("\nSuccessfully Logged in as: %s, welcome %s! uid: %d", user_role, name[0], uid);
        
        do{
            System.out.print("\n================================================================================================================================================================");
            System.out.print("\nDate: "+date.toString()
                    + "\n\n"
                    + "╒===================╕\n"
                    + "│ Main menu         │\n"
                    + "├-------------------┤\n"
                    + "│[1]| Profile       │ \n"
                    + "│[2]| Projects      │\n"
                    + "│[3]| Tasks         │\n"
                    + "│[4]| Teams         │\n"
                    + "│[5]| Team Members  │\n"
                    + "│[6]| Users         │\n"
                    + "│[7]| Logout        │\n"
                    + "│[0]| Exit          │\n"
                    + "└-------------------┘\n"
                    + "\n|Enter selection: ");
            int select = validate.validateInt();
            
            switch(select){
                case 1:
                    u.viewProfile(uid);
                    break;
                case 2:
                    proj.projectInterface(uid, name[0]);
                    break;
                case 3:
                    tl.taskListInterface(uid);
                    break;
                case 4:
                    tm.teamInterface();
                    break;
                case 5:
                    tmm.memberInterface();
                    break;
                case 6:
                    usr.userInterface();
                    break;
                case 7:
                    isSelected = true;
                    break;
                case 0:
                    exitMessage();
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
        } while(!isSelected);
    }
}
