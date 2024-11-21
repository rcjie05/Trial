package Users;

import Main.validation;
import Main.config;
import Crud.userCRUD;
import Crud.taskCRUD;
import Crud.teamMemberCRUD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Member extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    userCRUD u = new userCRUD();
    taskCRUD tsk = new taskCRUD();
    teamMemberCRUD tm = new teamMemberCRUD();
    
    String sql;
    
    public void displayInterface(int uid, String user_role, String first_name) throws IOException{
        String[] name = first_name.split(" ");
        System.out.printf("\nSuccessfully Logged in as: %s, welcome %s! uid: %d", user_role, name[0], uid);
        
        boolean isSelected = false;
        
        do{
            System.out.print("\n================================================================================================================================================================"
                    + "\nDate: "+date.toString()
                    + "\n\nMain Menu:"
                    + "\n1. Profile"
                    + "\n2. Tasks"
                    + "\n3. Teams"
                    + "\n4. Logout"
                    + "\n0. Exit"
                    + "\nEnter selection: ");
            int mainSelect = validate.validateInt();
            
            switch(mainSelect){
                case 1:
                    u.viewProfile(uid);
                    break;
                case 2:
                    Tasks(first_name, uid);
                    break;
                case 3:
                    System.out.println("\nAssigned teams:");
                    sql = "SELECT t.team_id, t.team_name, tm.member_name "
                            + "FROM team_member tm "
                            + "INNER JOIN team t ON tm.team_id = t.team_id "
                            + "WHERE tm.member_name = ?";
                    
                    if(!tableValidate(sql, first_name))
                        tm.viewAssignedTeams(first_name, sql);
                    pause();
                    break;
                case 4:
                    isSelected = true;
                    break;
                case 0:
                    exitMessage();
                default: System.out.println("Error: Invalid selection.");
            }
        } while(!isSelected);
    }
    
    private boolean tableValidate(String query, String getName){
        try{
            PreparedStatement findData = connectDB().prepareStatement(query);
            findData.setString(1, getName);
            
            try(ResultSet result = findData.executeQuery()){
                if(!result.next()){
                    System.out.println("--------------------------------------------------------------------------------");
                    System.out.println("Table Empty.");
                    return true;
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
    
    private int getTaskID(String getName){
        try{
            PreparedStatement findTask = connectDB().prepareStatement("SELECT task_id "
                    + "FROM task "
                    + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                    + "WHERE tm.member_name = ?");
            findTask.setString(1, getName);
            
            try(ResultSet result = findTask.executeQuery()){
                return result.getInt("task_id");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return 0;
    }
    
    private void Tasks(String first_name, int uid) throws IOException{
        String confirm;
        boolean taskSelected = false;
                    
        do{
            System.out.println("\nAssigned tasks:");
             sql = "SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                     + "FROM task "
                     + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                     + "INNER JOIN project p ON task.project_id = p.project_id "
                     + "WHERE tm.member_name = ? AND task.status = 'Not Started' OR task.status = 'In-Progress'";

             if(!tableValidate(sql, first_name)){
                 tsk.viewTaskFiltered(first_name, sql);
                 System.out.print("1. View full info"
                         + "\n2. Start a task"
                         + "\n3. Finish task"
                         + "\n4. Cancel task"
                         + "\n5. View completed tasks"
                         + "\n6. Back"
                         + "\nEnter selection: ");
                 int taskSelect = validate.validateInt();

                 switch(taskSelect){
                     case 1:
                         sql = "SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                                 + "FROM task "
                                 + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                                 + "INNER JOIN project p ON task.project_id = p.project_id "
                                 + "WHERE tm.member_name = ?";

                         System.out.println("\nAssigned tasks:");
                         tsk.viewTaskFiltered(first_name, sql);

                         System.out.print("Enter task ID: ");
                         int taskID = validate.validateInt();

                         while(getSingleValue("SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                                 + "FROM task "
                                 + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                                 + "INNER JOIN project p ON task.project_id = p.project_id "
                                 + "WHERE tm.member_name = ? AND task_id = ?", first_name, taskID) == 0)
                         {
                             System.out.print("Error: Task not on the list, try again: ");
                             taskID = validate.validateInt();
                         }
                         tsk.getTaskInfo(taskID);
                         pause();
                         break;
                     case 2:
                         if(getSingleValue("SELECT user_id "
                                 + "FROM team_member WHERE status = ? AND user_id = ?", "Available", uid) == 0){
                             System.out.println("Error: You already started a task.");
                         }
                         else{
                             System.out.println("\nAssigned tasks:");
                             tsk.viewTaskFiltered(first_name, sql);

                             System.out.print("Enter task ID: ");
                             taskID = validate.validateInt();

                             while(getSingleValue("SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                                     + "FROM task "
                                     + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                                     + "INNER JOIN project p ON task.project_id = p.project_id "
                                     + "WHERE tm.member_name = ? AND task_id = ? AND task.status = 'Not Started' OR task.status = 'In-Progress'", first_name, taskID) == 0)
                             {
                                 System.out.print("Error: Task not on the list, try again: ");
                                 taskID = validate.validateInt();
                             }

                             tsk.getTaskInfo(taskID);

                             System.out.print("Start task? [y/n]: ");
                             confirm = sc.next();

                             if(validate.confirm(confirm)){
                                 updateRecord("UPDATE task SET status = 'In-Progress' WHERE task_id = ?", taskID);
                                 updateRecord("UPDATE team_member SET status = 'Unavailable' WHERE member_name = ?", first_name);
                                 System.out.println("Task successfully started.");
                             } else{
                                 System.out.println("Transaction cancelled.");
                             }
                         }
                         break;
                     case 3:
                         if(getSingleValue("SELECT user_id "
                                 + "FROM team_member WHERE status = ? AND user_id = ?", "Unavailable", uid) == 0){
                             System.out.println("Error: You haven't started a task.");
                         }
                         else{
                             int tid = getTaskID(first_name);

                             System.out.println("\nAssigned task:");
                             tsk.getTaskInfo(tid);

                             System.out.print("Finish task? [y/n]: ");
                             confirm = sc.next();

                             if(validate.confirm(confirm)){
                                 updateRecord("UPDATE task SET status = 'Completed' WHERE task_id = ?", tid);
                                 updateRecord("UPDATE team_member SET status = 'Available' WHERE member_name = ?", first_name);
                                 System.out.println("Task succesfully finished.");
                             }
                             else{
                                 System.out.println("Transaction cancelled.");
                             }
                         }
                         break;
                     case 4:
                         if(getSingleValue("SELECT user_id "
                                 + "FROM team_member WHERE status = ? AND user_id = ?", "Unavailable", uid) == 0){
                             System.out.println("Error: You haven't started a task.");
                         }
                         else{
                             int tid = getTaskID(first_name);

                             System.out.println("\nAssigned task:");
                             tsk.getTaskInfo(tid);

                             System.out.print("Cancel task? [y/n]: ");
                             confirm = sc.next();

                             if(validate.confirm(confirm)){
                                 updateRecord("UPDATE task SET status = 'Not Started' WHERE task_id = ?", tid);
                                 updateRecord("UPDATE team_member SET status = 'Available' WHERE member_name = ?", first_name);
                                 System.out.println("Task succesfully cancelled.");
                             }
                             else{
                                 System.out.println("Transaction cancelled.");
                             }
                         }
                         break;
                     case 5:
                         sql = "SELECT task.task_id, task.task_name, task.due_date, tm.member_name, p.project_name, task.status "
                             + "FROM task "
                             + "INNER JOIN team_member tm ON task.assigned_to = tm.team_member_id "
                             + "INNER JOIN project p ON task.project_id = p.project_id "
                             + "WHERE tm.member_name = ? AND task.status = 'Completed'";
                         tsk.viewTaskFiltered(first_name, sql);
                         pause();
                         break;
                     case 6:
                         taskSelected = true;
                         break;
                     default: System.out.println("Error: Invalid selection.");
                 } 
             }
         } while(!taskSelected);
    }
}
