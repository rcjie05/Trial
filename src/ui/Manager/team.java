package ui.Manager;

import Main.validation;
import Main.config;
import Crud.teamCRUD;

import java.io.IOException;

public class team extends config {
    
    validation validate = new validation();
    
    boolean isSelected = false;
    
    public void teamInterface() throws IOException{
        teamCRUD t = new teamCRUD();
        
        do{
            System.out.println("================================================================================================================================================================");
            t.viewTeam();
            
            System.out.print("1. Add team"
                    + "\n2. Edit team"
                    + "\n3. Delete team"
                    + "\n4. View team info"
                    + "\n5. Filter by"
                    + "\n6. Back"
                    + "\n0. Exit"
                    + "\nEnter selection: ");
            int choice = validate.validateInt();

            switch(choice){
                case 1:
                    t.addTeam();
                    break;
                case 2:
                    t.editTeam();
                    break;
                case 3:
                    t.deleteTeam();
                    break;
                case 4:
                    t.viewInfo();
                    break;
                case 5:
                    t.filterBy();
                    break;
                case 6:
                    isSelected = true;
                    break;
                case 0:
                    exitMessage();
                    break;
                default:
                    System.out.println("Error: Invalid Selection.");
            }
        } while(!isSelected);
    }
}
