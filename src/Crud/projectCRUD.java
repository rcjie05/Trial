package Crud;

import Main.validation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import Main.config;

public class projectCRUD extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    
    String sql;
    
    public void viewProject(){
        System.out.println("List of Projects: ");
        
        String projectQuery = "SELECT * FROM project";
        String[] projectHeaders = {"ID", "Name", "Due Date", "Status"};
        String[] projectColumns = {"project_id", "project_name", "due_date", "status"};
        
        try{
            PreparedStatement findRow = connectDB().prepareStatement(projectQuery);
            
            try (ResultSet checkRow = findRow.executeQuery()) {
                if(!checkRow.next()){
                    System.out.println("Table empty.");
                }
                else{
                    viewRecords(projectQuery, projectHeaders, projectColumns);
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void addProject(int uid, String mname){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("Enter project name: ");
        String name = sc.nextLine();

        System.out.println("Enter description: ");
        String desc = sc.nextLine();

        System.out.print("Enter due date [FORMAT: YYYY-MM-DD]: ");
        String dueDate = sc.nextLine();
        
        while(validate.spaceValidate(dueDate)){
            dueDate = sc.nextLine();
        }
        
        while(!(validate.dateValidate(dueDate))){
            dueDate = sc.nextLine();
        }
        
        if(!checkSkip(name, desc, dueDate, uid, mname)){
            sql = "INSERT INTO project (project_name, description, date_created, due_date, project_manager_id, manager_name, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'Planned')";
        addRecord(sql, name, desc, date.toString(), dueDate, uid, mname);
        }
    }
    
    public void editProject(){
        boolean isSelected = false;
        
        System.out.print("Enter project ID: ");
        int id = validate.validateInt();
        
        while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", id) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            id = validate.validateInt();
        }
        
        getProjectInfo(id);
        
        do{
            System.out.print("Choose what you want to do:"
                    + "\n1. Change project name\n"
                    + "2. Change project description\n"
                    + "3. Change due date\n"
                    + "4. Change status\n"
                    + "5. Back\n"
                    + "Enter selection: ");
            int selectEdit = validate.validateInt();

            switch(selectEdit){
                case 1:
                    System.out.print("\nEnter new project name: ");
                    String newName = sc.nextLine();
                    updateRecord("UPDATE project SET project_name = ? WHERE project_id = ?", newName, id);
                    break;
                case 2:
                    System.out.print("\nEnter new project description: ");
                    String newDesc = sc.nextLine();
                    
                    updateRecord("UPDATE project SET description = ? WHERE project_id = ?", newDesc, id);
                    break;
                case 3:
                    System.out.print("\nEnter new due date [FORMAT: YYYY-MM-DD]: ");
                    String newDate = sc.nextLine();

                    while(validate.spaceValidate(newDate)){
                        newDate = sc.nextLine();
                    }

                    while(validate.dateValidate(newDate)){
                        newDate = sc.nextLine();
                    }
                    
                    updateRecord("UPDATE project SET due_date = ? WHERE project_id = ?", newDate, id);
                    break;
                case 4:
                System.out.print("\nEnter new status[Planned/In-Progress/Completed/Overdue]: ");
                String newStatus = sc.nextLine();

                while(validate.spaceValidate(newStatus)){
                    newStatus = sc.nextLine();
                }

                while(statusValidate(newStatus)){
                    newStatus = sc.nextLine();
                }
                
                updateRecord("UPDATE project SET status = ? WHERE project_id = ?", newStatus, id);
                break;
                case 5:
                    isSelected = true;
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
            System.out.println("");
        } while(!isSelected);
    }
    
    public void deleteProject(){
        System.out.print("Enter project ID: ");
        int id = validate.validateInt();
        
        while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", id) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            id = validate.validateInt();
        }
        
        getProjectInfo(id);
        
        System.out.print("Confirm Delete? [y/n]: ");
        String confirm = sc.nextLine();

        while(validate.spaceValidate(confirm)){
            confirm = sc.nextLine();
        }

        if(validate.confirm(confirm)){
            deleteRecord("DELETE FROM project WHERE project_id = ?", id);
        } else{
            System.out.println("Deletion Cancelled.");
        }
        
    }
    
    public void FilterBy() throws IOException{
        boolean isBack = false;
        do{
            System.out.print("\nFilter by: "
                    + "\n1. Due date"
                    + "\n2. Status"
                    + "\n3. Back"
                    + "\nEnter selection: ");
            int filterSelect = validate.validateInt();

            switch(filterSelect){
                case 1:
                    System.out.print("Enter due date [YYYY-MM-DD]: ");
                    String getDate = sc.nextLine();
                    
                    while(validate.spaceValidate(getDate)){
                        getDate = sc.nextLine();
                    }

                    while(!checkDate(getDate)){
                        getDate = sc.nextLine();
                    }

                    System.out.println("\nTask list fileted by: Date");
                    viewProjectFiltered(getDate,"SELECT * FROM project WHERE due_date = ?");
                    break;
                case 2:
                    System.out.print("Enter status [Planned/In-Progress/Completed/Overdue]: ");
                    String getStatus = sc.nextLine();
                    
                    while(validate.spaceValidate(getStatus)){
                        getStatus = sc.nextLine();
                    }

                    while(statusValidate(getStatus)){
                        getStatus = sc.nextLine();

                        while(validate.spaceValidate(getStatus)){
                            getStatus = sc.nextLine();
                        }
                    }

                    System.out.println("\nTask list fileted by: Status");
                    viewProjectFiltered(getStatus, "SELECT * FROM project WHERE status = ?");
                    break;
                case 3:
                    isBack = true;
                    break;
                default: System.out.println("Error: Invalid Selection.");
            }
        } while(!isBack);
    }
    
    public void viewProjectInfo() throws IOException{
        System.out.print("Enter project ID: ");
        int id = validate.validateInt();
        
        while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", id) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            id = validate.validateInt();
        }
        
        sql = "SELECT p.project_id, p.project_name, p.description, p.date_created, p.due_date, p.manager_name, p.status "
                + "FROM project p "
                + "INNER JOIN user u ON project_manager_id = u.user_id "
                + "WHERE project_id = ?";
        getProjectInfo(id);
        viewList(id);
        
    }
    
    public void updateStatus(){
        try{
            PreparedStatement state = connectDB().prepareStatement("SELECT * FROM project");
            ResultSet result = state.executeQuery();
            
            while(result.next()){
                int getID = result.getInt("project_id");
                String dueDate = result.getString("due_date");
                String projectName = result.getString("project_name");
                String getStatus = result.getString("status");
                
                if(!getStatus.equals("Overdue") && !getStatus.equals("Completed")
                        && date.isAfter(LocalDate.parse(dueDate))){

                        result.close();
                        sql = "UPDATE project SET status = 'Overdue' WHERE project_id = ?";
                        updateRecord(sql, getID);

                        System.out.println("Project "+projectName+" is passed due date, status updated to Overdue.");
                }
            }
            result.close();
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
            
    }
    
    private void getProjectInfo(int id){
        try{
            PreparedStatement findRow = connectDB().prepareStatement("SELECT p.project_id, p.project_name, p.description, p.date_created, p.due_date, p.manager_name, p.status "
                    + "FROM project p "
                    + "INNER JOIN user u ON project_manager_id = u.user_id "
                    + "WHERE project_id = ?;");
            findRow.setInt(1, id);
            
            try (ResultSet result = findRow.executeQuery()) {
                
                System.out.println("--------------------------------------------------------------------------------"
                        + "\nSelected project:  | "+result.getString("project_name")
                        + "\n-------------------+ "
                        + "\nProject ID:        | "+result.getInt("project_id")
                        + "\nDescription:       | "+result.getString("description")
                        + "\nDate Created:      | "+result.getString("date_created")
                        + "\nDue Date:          | "+result.getString("due_date")
                        + "\nProject Manager:   | "+result.getString("manager_name")
                        + "\nStatus:            | "+result.getString("status")
                        + "\n--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private void viewList(int pid) throws IOException{
        taskCRUD tsk = new taskCRUD();
        teamCRUD tm = new teamCRUD();
        try{
            PreparedStatement search = connectDB().prepareStatement("SELECT p.project_name, p.description, p.date_created, p.due_date, u.first_name, p.status FROM project p "
                    + "INNER JOIN user u ON project_manager_id = u.user_id "
                    + "WHERE project_id = ?;");
            search.setInt(1, pid);
            
            try(ResultSet result = search.executeQuery()){
                
                System.out.println("\nTeam list:");
                sql = "SELECT t.team_id, t.team_name, p.project_name "
                        + "FROM team t "
                        + "INNER JOIN project p ON t.project_id = p.project_id "
                        + "WHERE p.project_name = ?";
                System.out.println("List of teams working on this project: ");
                
                tm.viewTeamFiltered(result.getString("project_name"), sql);

                System.out.println("\nTask list:");
                sql = "SELECT t.task_id, t.task_name, t.due_date, u.first_name, p.project_name, t.status "
                        + "FROM task t "
                        + "INNER JOIN user u ON t.assigned_to = u.user_id "
                        + "INNER JOIN project p ON t.project_id = p.project_id "
                        + "WHERE p.project_name = ?";
                
                tsk.viewTaskFiltered(result.getString("project_name"), sql);
            }
            pause();
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        System.out.println("\nTeam list: ");
    }
    
    private void viewProjectFiltered (String getColumn, String query) throws IOException{
        try{
            PreparedStatement filter = connectDB().prepareStatement(query);

            filter.setString(1, getColumn);
            try (ResultSet checkRow = filter.executeQuery()) {
                System.out.println("--------------------------------------------------------------------------------");
                System.out.printf("%-20s %-20s %-20s %-20s\n", "ID", "Name", "Due Date", "Status");
                
                while(checkRow.next()){
                    int pid = checkRow.getInt("project_id");
                    String pname = checkRow.getString("project_name");
                    String pdue = checkRow.getString("due_date");
                    String pstats = checkRow.getString("status");
                    
                    System.out.printf("%-20d %-20s %-20s %-20s\n", pid, pname, pdue, pstats);
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private boolean statusValidate(String getStatus){
        String[] status = {"Planned", "In-Progress", "Completed", "Overdue"};
        
        for(String i : status){
            if(getStatus.equals(i)){
                return false;
            }
        }
        System.out.print("Error: Status must be (Planned/In-Progress/Completed/Overdue), try again: ");
        return true;
    }
    
    private boolean checkSkip(String getName, String getDesc, String getDue, int mid, String Manager){
        try{
            PreparedStatement findID = connectDB().prepareStatement("SELECT project_id FROM project ORDER BY project_id");
            ResultSet getID = findID.executeQuery();
            
            int previousId = 0;
            
            while (getID.next()) {
                int currentId = getID.getInt("project_id");
                
                if (previousId != 0 && currentId != previousId + 1) {
                    getID.close();
                    sql = "INSERT INTO project (project_id, project_name, description, date_created, due_date, project_manager_id, manager_name, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, 'Planned')";
                    addRecord(sql, (currentId-1), getName, getDesc, date.toString(), getDue, mid, Manager);
                    return true;
                }
                previousId = currentId;
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
    
    private boolean checkDate(String getDate){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try{
            LocalDate.parse(getDate, format);
            return true;
        } catch(DateTimeParseException e){
            System.out.print("Error: Date is incorrect, try again: ");
        }
        return false;
    }
}
