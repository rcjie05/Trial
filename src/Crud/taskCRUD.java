package Crud;

import Main.config;
import Main.validation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class taskCRUD extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    
    String sql;
    
    public void viewTask(){
        sql = "SELECT task_id, task_name, task.due_date, p.manager_name, tm.member_name, p.project_name, task.status "
                + "FROM task "
                + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                + "INNER JOIN project p ON task.project_id = p.project_id "
                + "WHERE task.status = 'In-Progress' OR task.status = 'Not Started'";
        try{
            PreparedStatement findRow = connectDB().prepareStatement(sql);
            
            try (ResultSet checkTable = findRow.executeQuery()) {
                if(!checkTable.next()){
                    System.out.println("Task Empty.");
                } else{
                    System.out.println("List of Tasks: ");
                    viewTaskList(sql);
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void addTask(int id){
        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("Enter task name: ");
        String Tname = sc.nextLine();

        System.out.print("Enter task description: ");
        String desc = sc.nextLine();

        System.out.print("Enter due date [FORMAT: YYYY-MM-DD]: ");
        String dueDate = sc.nextLine();
        
        String dateCreated = date.toString();
        
        while(validate.spaceValidate(dueDate)){
            dueDate = sc.nextLine();
        }
        
        while(!(validate.dateValidate(dueDate))){
            dueDate = sc.nextLine();
        }
        
        System.out.print("Enter project ID to assign: ");
        int pid = validate.validateInt();
        
        while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", pid) == 0){
            System.out.print("ERROR: ID doesn't exist, try again: ");
            pid = validate.validateInt();
        }
        
        if(!checkSkip(Tname, desc, dateCreated, dueDate, id, pid)){
            sql = "INSERT INTO task (task_name, description, date_created, due_date, created_by, assigned_to, project_id, status) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?, 'Not Started')";
            addRecord(sql, Tname, desc, dateCreated, dueDate, id, pid);
        }
    }
    
    public void editTask(){
        boolean Selected = false;
        
        System.out.print("\nEnter ID: ");
        int taskID = validate.validateInt();
        
        while(getSingleValue("SELECT task_id FROM task WHERE task_id = ?", taskID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            taskID = validate.validateInt();
        }
        
        getTaskInfo(taskID);
        
        do{
            System.out.print("1. Change task name"
                    + "\n2. Change task description"
                    + "\n3. Change due date"
                    + "\n4. Change status"
                    + "\n5. Remove asignee"
                    + "\n6. Back"
                    + "\nEnter selection: ");
            int selectEdit = validate.validateInt();
            
            switch(selectEdit){
                case 1:
                    System.out.print("\nEnter new task name: ");
                    String newName = sc.nextLine();
                    
                    updateRecord("UPDATE task SET task_name = ? WHERE task_id = ?", newName, taskID);
                    break;
                case 2:
                    System.out.println("\nEnter new task description: ");
                    String newDesc = sc.nextLine();
                    
                    updateRecord("UPDATE task SET description = ? WHERE task_id = ?", newDesc, taskID);
                    break;
                case 3:
                    System.out.print("\nEnter new due date [FORMAT: YYYY-MM-DD]: ");
                    String newDate = sc.nextLine();
                    
                    while(validate.spaceValidate(newDate)){
                        newDate = sc.nextLine();
                    }

                    while(!(validate.dateValidate(newDate))){
                        newDate = sc.nextLine();
                    }
                    
                    updateRecord("UPDATE task SET due_date = ? WHERE task_id = ?", newDate, taskID);
                    break;
                case 4:
                    System.out.print("\nEnter new status [Not Started/In Progress/Completed/Overdue]: ");
                    String newStatus = sc.nextLine();

                    while(statusValidate(newStatus)){
                        newStatus = sc.nextLine();
                    }
                    
                    updateRecord("UPDATE task SET status = ? WHERE task_id = ?", newStatus, taskID);
                    break;
                case 5:
                    if(checkAsignee(taskID)){
                        System.out.println("Error: This task isn't assigned to any member, removal cancelled.");
                    }
                    else if(getSingleValue("SELECT task_id FROM task WHERE task_id = ? AND status = 'In-Progress'", taskID) != 0){
                        System.out.println("Error: Member is working on this task, removal cancelled.");
                    } 
                    else{
                        System.out.print("Confirm remove? [y/n]: ");
                        String confirm = sc.nextLine();

                        if(validate.confirm(confirm)){
                            updateRecord("UPDATE task SET assigned_to = 0 WHERE task_id = ?", taskID);
                        } else{
                            System.out.println("Cancelled removal.");
                        }
                    }
                    break;
                case 6:
                    Selected = true;
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
            System.out.println("--------------------------------------------------------------------------------");
        } while(!Selected);
    }
    
    public void deleteTask(){
        System.out.print("\nEnter ID: ");
        int taskID = validate.validateInt();
        
        while(getSingleValue("SELECT task_id FROM task WHERE task_id = ?", taskID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            taskID = validate.validateInt();
        }
        
        getTaskInfo(taskID);
        
        System.out.print("Confirm delete? [y/n]: ");
        String confirm = sc.next();
        
        if(validate.confirm(confirm)){
            sql = "DELETE FROM task WHERE task_id = ?";
            deleteRecord(sql, taskID);
        } else{
            System.out.println("Deletion cancelled.");
        }
    }
    
    public void assignMember(){
        System.out.println("");
        
        try{
            PreparedStatement findRow = connectDB().prepareStatement("SELECT team_id FROM team");
            
            try(ResultSet result = findRow.executeQuery()){
                if(!result.next()){
                    System.out.println("Team table is empty.");
                } else{
                    result.close();
                    getTeamMember();
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private void getTeamMember(){
        teamCRUD t = new teamCRUD();
        teamMemberCRUD tm = new teamMemberCRUD();
        
        t.viewTeam();
                    
        System.out.print("Enter team ID: ");
        int tid = validate.validateInt();

        while(getSingleValue("SELECT team_id FROM team WHERE team_id = ?", tid) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            tid = validate.validateInt();
        }

        sql = "SELECT team_member_id, member_name, team_member.team_id, t.team_name, status "
                + "FROM team_member "
                + "INNER JOIN team t ON team_member.team_id = t.team_id "
                + "WHERE team_member.team_id = ?";

        try{
            PreparedStatement findTeamMembers = connectDB().prepareStatement(sql);
            findTeamMembers.setInt(1, tid);
            try(ResultSet getTeamMembers = findTeamMembers.executeQuery()){
                if(!getTeamMembers.next()){
                    System.out.println("Team member list empty.");
                } else{
                    getTeamMembers.close();
                    System.out.println("\nTeam members: ");
                    tm.viewTeamMemberFiltered(sql, tid);  

                    System.out.print("Enter member ID: ");
                    int mid = validate.validateInt();

                    while(getSingleValue("SELECT team_member_id FROM team_member WHERE team_member_id = ? AND team_id = ?", mid, tid) == 0){
                        System.out.print("Error: ID doesn't exist, try again: ");
                        mid = validate.validateInt();
                    }

                    findTeamMember(mid);
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private void findTeamMember(int mid){
        teamMemberCRUD tm = new teamMemberCRUD();
        
        if(getSingleValue("SELECT team_member_id FROM team_member WHERE status = ? AND team_member_id = ?", "Available", mid) == 0){
            System.out.print("Error: Member is busy.\n");
        }
        else{
            System.out.println("--------------------------------------------------------------------------------");
            tm.searchMember(mid);

            sql = "SELECT t.task_id, t.task_name, t.due_date, p.manager_name, tm.member_name, p.project_name, t.status FROM task t "
                    + "JOIN team_member tm ON t.assigned_to = tm.team_member_id "
                    + "JOIN project p ON t.project_id = p.project_id "
                    + "WHERE tm.member_name = ?";
            System.out.println("Available tasks: ");
            viewTaskFiltered("None", sql);

            System.out.print("Enter task ID: ");
            int task = validate.validateInt();

            while(getSingleValue("SELECT task_id FROM task INNER JOIN team_member tm ON assigned_to = tm.team_member_id WHERE task_id = ? AND tm.member_name = ?", task, "None") == 0){
                System.out.print("ERROR: ID doesn't exist, try again: ");
                task = validate.validateInt();
            }

            getTaskInfo(task);

            System.out.print("Confirm assign member? [y/n]: ");
            String confirm = sc.nextLine();

            if(validate.confirm(confirm)){
                sql = "UPDATE task SET assigned_to = ? WHERE task_id = ?";
                updateRecord(sql, mid, task);
            } else{
                System.out.print("Assign cancelled.");
            }
        }
    }
    
    public void viewInfo() throws IOException{
        System.out.print("\nEnter ID: ");
        int taskID = validate.validateInt();
        
        while(getSingleValue("SELECT task_id FROM task WHERE task_id = ?", taskID) == 0){
            System.out.print("ERROR: ID doesn't exist, try again: ");
            taskID = validate.validateInt();
        }
        
        getTaskInfo(taskID);
        System.out.print("Press any key to continue...");
        System.in.read();
    }
    
    public void filterBy(){
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

                    System.out.println("Task list filtered by: Date");
                    sql = "SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                            + "FROM task "
                            + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                            + "INNER JOIN project p ON task.project_id = p.project_id "
                            + "WHERE task.due_date = ?";
                    viewTaskFiltered(getDate, sql);
                    break;
                case 2:
                    System.out.print("Enter status [Not Started/In-Progress/Completed]: ");
                    String getStatus = sc.nextLine();

                    while(statusValidate(getStatus)){
                        getStatus = sc.nextLine();
                    }

                    System.out.println("Task list fileted by: Status");
                    sql = "SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                            + "FROM task "
                            + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                            + "INNER JOIN project p ON task.project_id = p.project_id "
                            + "WHERE task.status = ?";
                    viewTaskFiltered(getStatus, sql);
                    break;
                case 3:
                    isBack = true;
                    break;
                default: System.out.println("Error: Invalid selection.");
            }
        } while(!isBack);
    }
    
    private void viewTaskList(String query){
        String[] taskHeaders = {"ID", "Name", "Due Date", "Project", "Status"};
        String[] taskColumns = {"task_id", "task_name", "due_date", "project_name", "status"};
        
        viewRecords(query, taskHeaders, taskColumns);
    }
    
    public void viewTaskFiltered(String getColumn, String query){
        try{
            PreparedStatement filter = connectDB().prepareStatement(query);
            filter.setString(1, getColumn);
            
            try (ResultSet checkRow = filter.executeQuery()) {
                System.out.println("--------------------------------------------------------------------------------");
                System.out.printf("%-20s %-20s %-20s %-20s %-20s\n", "ID", "Name", "Due Date", "Project", "Status");
                while(checkRow.next()){
                    int t_id = checkRow.getInt("task_id");
                    String t_name = checkRow.getString("task_name");
                    String d_date = checkRow.getString("due_date");
                    String p_name = checkRow.getString("project_name");
                    String t_status = checkRow.getString("status");
                    
                    System.out.printf("%-20d %-20s %-20s %-20s %-20s\n", t_id, t_name, d_date, p_name, t_status);
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void getTaskInfo(int id){
        try{
            PreparedStatement search = connectDB().prepareStatement("SELECT t.task_id, t.task_name, t.description, t.date_created, t.due_date, p.manager_name, tm.member_name, p.project_name, t.status "
                    + "FROM task t "
                    + "JOIN team_member tm ON t.assigned_to = tm.team_member_id "
                    + "JOIN project p ON t.project_id = p.project_id WHERE t.task_id = ?;");
            search.setInt(1, id);
            
            try (ResultSet result = search.executeQuery()) {
                System.out.println("--------------------------------------------------------------------------------"
                        + "\nSelected task:     | "+result.getString("task_name")
                        + "\n-------------------+"
                        + "\nTask ID:           | "+result.getInt("task_id")
                        + "\nDescription:       | "+result.getString("description")
                        + "\nDate created:      | "+result.getString("date_created")
                        + "\nDue date:          | "+result.getString("due_date")
                        + "\nCreated By:        | "+result.getString("manager_name")
                        + "\nAssigned to:       | "+result.getString("member_name")
                        + "\nFrom project:      | "+result.getString("project_name")
                        + "\nStatus:            | "+result.getString("status")
                        + "\n--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private boolean statusValidate(String getStatus){
        String[] status = {"Not Started", "In-Progress", "Completed"};
        
        for(String i : status){
            if(getStatus.equals(i)){
                return false;
            }
        }
        System.out.print("Error: Status must be (Not Started/In-Progress/Completed/Overdue), try again: ");
        return true;
    }
    
    private boolean checkSkip(String getName, String getDesc, String getCreationDate, String getDueDate, int getID, int getProjectID){
        try{
            PreparedStatement findID = connectDB().prepareStatement("SELECT task_id FROM task ORDER BY task_id");
            ResultSet ID = findID.executeQuery();
            
            int previousId = 0;
            
            while(ID.next()){
                int currentId = ID.getInt("task_id");
                
                if(previousId != 0 && currentId != previousId + 1){
                    ID.close();
                    
                    sql = "INSERT INTO task (task_id, task_name, description, date_created, due_date, created_by, assigned_to, project_id, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, 'Not Started')";
                addRecord(sql, (currentId-1), getName, getDesc, getCreationDate, getDueDate, getID, getProjectID);
                return true;
                }
                previousId = currentId;
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
    
    private boolean checkAsignee(int getTaskID){
        try{
            PreparedStatement findRow = connectDB().prepareStatement("SELECT tm.member_name "
                    + "FROM task INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                    + "WHERE task_id = ?");
            findRow.setInt(1, getTaskID);
            
            ResultSet result = findRow.executeQuery();
            String getName = result.getString("member_name");
            
            if(getName.equals("None")){
                return true;
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
