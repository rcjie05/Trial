package Crud;

import Main.validation;
import Main.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Scanner;

public class userCRUD extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    
    String Query;
    
    public void viewUser(){
        Query = "SELECT * FROM user";
        String[] Header = {"ID", "Name", "Role"};
        String[] Column = {"user_id", "first_name", "role"};
        
        try{
            PreparedStatement findRow = connectDB().prepareStatement(Query);
            
            try (ResultSet checkRow = findRow.executeQuery()) {
                if(!checkRow.next()){
                    System.out.println("Table empty.");
                } else{
                    viewRecords(Query, Header, Column);
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void deleteUser(){
        System.out.print("\nEnter ID: ");
        int userID = validate.validateInt();
        
        while(getSingleValue("SELECT user_id FROM user WHERE user_id = ?", userID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            userID = validate.validateInt();
        }
        
        System.out.println("--------------------------------------------------------------------------------");
        searchUser(userID);
        
        System.out.print("Confirm delete? [y/n]: ");
        String confirm = sc.nextLine();
        
        if(validate.confirm(confirm)){
            deleteRecord("DELETE FROM user WHERE user_id = ?", userID);
        } else{
            System.out.println("Deletion cancelled.");
        }
    }
    
    public void viewUserInfo(int userID) throws IOException{
        System.out.println("--------------------------------------------------------------------------------");
        searchUser(userID);
        
        try{
            PreparedStatement state = connectDB().prepareStatement("SELECT * FROM user WHERE user_id = ?");
            state.setInt(1, userID);
            
            try (ResultSet result = state.executeQuery()) {
                System.out.println("\n"
                        + "╒===============================================================================╕\n"
                        + "│ Full Information                                                              │\n"
                        + "├--------------------┬---------------------------------------------------------┘"
                        + "\n│ User ID:           │ "+result.getInt("user_id")
                        + "\n│ First Name:        │ "+result.getString("first_name")
                        + "\n│ Middle Name:       │ "+result.getString("middle_name")
                        + "\n│ Last Name:         │ "+result.getString("last_name")
                        + "\n│ Email Address:     │ "+result.getString("email")
                        + "\n│ Username:          │ "+result.getString("username")
                        + "\n│ Password:          │ "+result.getString("password")
                        + "\n│ Role:              │ "+result.getString("role")
                        + "\n└--------------------┴----------------------------------------------------------");
                System.out.print("Press any key to continue...");
                System.in.read();
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void editUser(){
            System.out.print("\nEnter ID you want to edit: ");
            int userID = validate.validateInt();
            
            while(getSingleValue("SELECT user_id FROM user WHERE user_id = ?", userID) == 0){
                System.out.print("Error: ID doesn't exist, try again: ");
                userID = validate.validateInt();
            }

            System.out.println("--------------------------------------------------------------------------------");
            searchUser(userID);

            System.out.print("Select what you want to edit:"
                    + "\n1. Password"
                    + "\n2. Role"
                    + "\n3. Email Address"
                    + "\n4. First Name"
                    + "\n5. Middle Name"
                    + "\n6. Last Name"
                    + "\n7. Back"
                    + "\nEnter selection: ");
            int select = validate.validateInt();

            switch(select){
                case 1:
                    System.out.print("\nEnter new password: ");
                    String newPass = sc.nextLine();
                    
                    while(validate.spaceValidate(newPass)){
                        newPass = sc.nextLine();
                    }
                    
                    updateRecord("UPDATE user SET password = ? WHERE user_id = ?", newPass, userID);
                    break;
                case 2:
                    System.out.print("\nEnter new Role [admin/member/project manager]: ");
                    String newRole = sc.nextLine();
                    
                    while(roleValidate(newRole)){
                        newRole = sc.nextLine();
                    }
                    
                    Query = "UPDATE user SET role = ? WHERE user_id = ?";
                    updateRecord(Query, newRole, userID);
                    break;
                case 3:
                    System.out.print("\nEnter new email address: ");
                    String newEmail = sc.nextLine();
                    
                    while(validate.emailValidate(newEmail)){
                        newEmail = sc.nextLine();
                    }
                    
                    updateRecord("UPDATE user SET email = ? WHERE user_id = ?", newEmail, userID);
                    break;
                case 4:
                    System.out.print("\nEnter new first name: ");
                    String newFname = sc.nextLine();
                    
                    updateRecord("UPDATE user SET first_name = ? WHERE user_id = ?", newFname, userID);
                    break;
                case 5:
                    System.out.print("\nEnter new middle name: ");
                    String newMname = sc.nextLine();
                    
                    updateRecord("UPDATE user SET middle_name = ? WHERE user_id = ?", newMname, userID);
                    break;
                case 6:
                    System.out.print("\nEnter new last name: ");
                    String newLname = sc.nextLine();
                    
                    updateRecord("UPDATE user SET middle_name = ? WHERE user_id = ?", newLname, userID);
                    break;
                case 7:
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
    }
    
    private boolean roleValidate(String getRole){
        String[] role = {"admin", "member", "project manager"};
        
        for(String i : role){
            if(getRole.equals(i)){
                return false;
            }
        }
        System.out.print("Error: Role must be (admin/member/project manager) try again: ");
        return true;
    }
    
    public void viewUserFiltered(String getColumn, String getQuery){
        try{
            PreparedStatement filter = connectDB().prepareStatement(getQuery);
            filter.setString(1, getColumn);
            
            try(ResultSet checkRow = filter.executeQuery()){
                System.out.println("--------------------------------------------------------------------------------");
                System.out.printf("%-20s %-20s %-20s\n", "User ID", "Name", "Role");
                while(checkRow.next()){
                    String[] name = checkRow.getString("first_name").split(" ");
                    int id = checkRow.getInt("user_id");
                    String uname = name[0];
                    String role = checkRow.getString("role");
                    
                    System.out.printf("%-20d %-20s %-20s\n", id, uname, role);
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: e"+e.getMessage());
        }
    }
    
    public void viewProfile(int uid) throws IOException{
        boolean isBack = false;

        do{
            viewUserInfo(uid);

            System.out.print("1. Edit Info\n"
                    + "2. Back\n"
                    + "Enter selection: ");
            int editSelect = validate.validateInt();

            switch(editSelect){
                case 1:
                    editInfo(uid);
                    break;
                case 2:
                    isBack = true;    
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
        } while(!isBack);
    }
    
    public void editInfo(int userID){
        String Query;
        
        System.out.print("\nSelect what you want to edit:"
                + "\n1. Password"
                + "\n2. Role"
                + "\n3. Email Address"
                + "\n4. First Name"
                + "\n5. Middle Name"
                + "\n6. Last Name"
                + "\n7. Back"
                + "\nEnter selection: ");
        int select = validate.validateInt();

        switch(select){
            case 1:
                System.out.print("\nEnter new password: ");
                String newPassword = sc.nextLine();

                while(validate.spaceValidate(newPassword)){
                    newPassword = sc.nextLine();
                }

                while(validatePassword(userID, newPassword)){
                    System.out.print("Error: Must not be an old password, try again: ");
                    newPassword = sc.nextLine();

                    while(validate.spaceValidate(newPassword)){
                        newPassword = sc.nextLine();
                    }
                }

                Query = "UPDATE user SET password = ? WHERE user_id = ?";
                updateRecord(Query, newPassword, userID);
                break;
            case 2:
                System.out.print("\nEnter new Role [admin/member/project manager]: ");
                String newRole = sc.nextLine();
                
                while(validate.spaceValidate(newRole)){
                    newRole = sc.nextLine();
                }
                
                while(!(validate.roleValidate(newRole))){
                    newRole = sc.nextLine();
                }

                Query = "UPDATE user SET role = ? WHERE user_id = ?";
                updateRecord(Query, newRole, userID);
                break;
            case 3:
                System.out.print("\nEnter new email address: ");
                String newEmail = sc.nextLine();
                
                while(validate.spaceValidate(newEmail)){
                    newEmail = sc.nextLine();
                }
                
                while(validate.emailValidate(newEmail)){
                    newEmail = sc.nextLine();
                }

                Query = "UPDATE user SET email = ? WHERE user_id = ?";
                updateRecord(Query, newEmail, userID);
                break;
            case 4:
                System.out.print("\nEnter new first name: ");
                String newFname = sc.nextLine();

                Query = "UPDATE user SET first_name = ? WHERE user_id = ?";
                updateRecord(Query, newFname, userID);
                break;
            case 5:
                System.out.print("\nEnter new middle name: ");
                String newMname = sc.nextLine();

                Query = "UPDATE user SET middle_name = ? WHERE user_id = ?";
                updateRecord(Query, newMname, userID);
                break;
            case 6:
                System.out.print("\nEnter new last name: ");
                String newLname = sc.nextLine();

                Query = "UPDATE user SET middle_name = ? WHERE user_id = ?";
                updateRecord(Query, newLname, userID);
                break;
            case 7:
                break;
            default: System.out.println("Error: Invalid selection.");
        }
    }
    
    private boolean validatePassword(int getID, String getPassword){
        try{
            PreparedStatement state = connectDB().prepareStatement("SELECT password FROM user WHERE user_id = ?");
            state.setInt(1, getID);
            ResultSet result = state.executeQuery();
            
            String pass = result.getString("password");
            result.close();
            
            if(getPassword.equals(pass))
            return true;
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
    
    public void searchUser(int id){
        try{
            PreparedStatement search = connectDB().prepareStatement("SELECT * FROM user WHERE user_id = ?");
            
            search.setInt(1,id);
            try (ResultSet result = search.executeQuery()) {
                String[] selectedName = result.getString("first_name").split(" ");
                
                System.out.println("Selected user: "+selectedName[0]);
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}
