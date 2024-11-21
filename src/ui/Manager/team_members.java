package ui.Manager;

import Main.validation;
import Main.config;
import Crud.teamMemberCRUD;

import java.io.IOException;

public class team_members extends config{
    
    validation validate = new validation();
    teamMemberCRUD tm = new teamMemberCRUD();
    
    boolean isSelected = false;
    int choice;
    
    public void memberInterface() throws IOException{
       do{
            System.out.println("================================================================================================================================================================");
            tm.viewTeam();
            
            System.out.print("1. Add member"
                    + "\n2. Replace member"
                    + "\n3. Remove member"
                    + "\n4. Select member"
                    + "\n5. Filter by"
                    + "\n6. Back"
                    + "\n0. Exit"
                    + "\nEnter selection: ");
            choice = validate.validateInt();
            
            switch(choice){
                case 1:
                    tm.addMember();
                    break;
                case 2:
                    tm.replaceMember();
                    break;
                case 3:
                    tm.removeMember();
                    break;
                case 4:
                    tm.selectMember();
                    break;
                case 5:
                    tm.filterBy();
                    break;
                case 6:
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
