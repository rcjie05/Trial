package Crud;

import Main.validation;
import Main.config;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class teamCRUD extends config {
    Scanner sc = new Scanner(System.in);
    
    validation validate = new validation();
    teamMemberCRUD tm = new teamMemberCRUD();
    
    String sql;
    
    public void viewTeam(){
        String sqlQuery = "SELECT team_id, team_name, team.project_id, p.project_name "
                + "FROM team "
                + "INNER JOIN project p ON team.project_id = p.project_id";
        
        try{
            PreparedStatement findRow = connectDB().prepareStatement(sqlQuery);
            
            try (ResultSet checkTable = findRow.executeQuery()) {
                if(!checkTable.next()){
                    System.out.println("Team List Empty");
                } else{
                    viewTeamList(sqlQuery);
                }
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    public void addTeam(){
       
        System.out.print("\nEnter team name: ");
        String team_name = sc.nextLine();

        System.out.print("Enter project ID to assign: ");
        int pid = validate.validateInt();
        
        while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", pid) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            pid = validate.validateInt();
        }
        
        if(!checkSkip(team_name, pid)){
            addRecord("INSERT INTO team(team_name, project_id) VALUES (?, ?)", team_name, pid);
        }
    }
    
    public void editTeam(){
        System.out.print("\nEnter ID to edit: ");
        int teamID = validate.validateInt();
        
        while(getSingleValue("SELECT team_id FROM team WHERE team_id = ?", teamID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            teamID = validate.validateInt();
        }
        
        getTeamInfo(teamID);
        
        System.out.print("1. Change Name"
                + "\n2. Change assigned project"
                + "\n3. Cancel"
                + "\nEnter selection: ");
        int editSelect = validate.validateInt();
        
        switch(editSelect){
            case 1:
                System.out.print("Enter new name: ");
                String newName = sc.nextLine();
                
                updateRecord("UPDATE team SET team_name = ? WHERE team_id = ?", newName, teamID);
                break;
            case 2:
                System.out.print("Enter project ID to assign: ");
                int newID = validate.validateInt();
                
                while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", newID) == 0){
                    System.out.print("Error: ID doesn't exist, try again: ");
                    newID = validate.validateInt();
                }

                sql = "UPDATE team SET project_id = ? WHERE team_id = ?";
                updateRecord(sql, newID, teamID);
                break;
            case 3:
                System.out.println("Update Cancelled.");
                break;
        }
    }
    
    public void deleteTeam(){
        System.out.print("\nEnter ID to delete: ");
        int teamID = validate.validateInt();
        
        while(getSingleValue("SELECT team_id FROM team WHERE team_id = ?", teamID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            teamID = validate.validateInt();
        }
        
        getTeamInfo(teamID);
        
        System.out.print("Confirm delete? [y/n]: ");
        String confirm = sc.nextLine();
        
        if(validate.confirm(confirm)){
            deleteRecord("DELETE FROM team WHERE team_id = ?", teamID);
        } else{
            System.out.println("Deletion cancelled.");
        }
    }
    
    public void viewInfo() throws IOException{
        System.out.print("Enter ID: ");
        int teamID = validate.validateInt();
        
        while(getSingleValue("SELECT team_id FROM team WHERE team_id = ?", teamID) == 0){
            System.out.print("Error: ID doesn't exist, try again: ");
            teamID = validate.validateInt();
        }
        
        getTeamInfo(teamID);

        sql = "SELECT tm.team_member_id, tm.member_name, tm.team_id, t.team_name, status "
                + "FROM team_member tm "
                + "INNER JOIN team t ON tm.team_id = t.team_id "
                + "WHERE tm.team_id = ?";
        System.out.println("\nTeam Members: ");
        tm.viewTeamMemberFiltered(sql, teamID);
        
        System.out.print("Press any key to continue...");
        System.in.read();
    }
    
    public void filterBy(){
        boolean isBack = false;
        
        do{
            System.out.print("\nFilter by:"
                    + "\n1. Project"
                    + "\n2. Back"
                    + "\nEnter selection: ");
            int filterSelect = validate.validateInt();

            switch(filterSelect){
                case 1:
                    System.out.print("Enter project ID: ");
                    int getProject = validate.validateInt();
                    
                    while(getSingleValue("SELECT project_id FROM project WHERE project_id = ?", getProject) == 0){
                        System.out.print("Error: ID doesn't exist, try again: ");
                        getProject = validate.validateInt();
                    }

                    System.out.println("\nTeam list filtered by:");
                    System.out.println("--------------------------------------------------------------------------------");
                    try{
                        PreparedStatement state = connectDB().prepareStatement("SELECT p.project_name "
                                + "FROM team t "
                                + "INNER JOIN project p ON t.project_id = p.project_id "
                                + "WHERE t.project_id = ?");

                        state.setInt(1, getProject);
                        
                        try (ResultSet result = state.executeQuery()) {
                            sql = "SELECT t.team_id, t.team_name, p.project_name "
                                    + "FROM team t "
                                    + "INNER JOIN project p ON t.project_id = p.project_id "
                                    + "WHERE p.project_name = ?";
                            System.out.println(result.getString("project_name"));

                            viewTeamFiltered(result.getString("project_name"), sql);
                        }
                    } catch(SQLException e){
                        System.out.println("Error: "+e.getMessage());
                    }
                    break;
                case 2:
                    isBack = true;
                    break;
                default: System.out.println("Error: Invalid selection");
            }
        } while(!isBack);
    }
    
    public void viewTeamFiltered(String getColumn, String query){
        try{
            PreparedStatement filter = connectDB().prepareStatement(query);
            filter.setString(1, getColumn);
            
            try (ResultSet checkRow = filter.executeQuery()) {
                System.out.println("--------------------------------------------------------------------------------");
                System.out.printf("%-20s %-20s %-20s\n", "ID", "Name", "Project");
                while(checkRow.next()){
                    int tid = checkRow.getInt("team_id");
                    String tname = checkRow.getString("team_name");
                    String pname = checkRow.getString("project_name");
                    
                    System.out.printf("%-20d %-20s %-20s\n", tid, tname, pname);
                }
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private void viewTeamList(String Query){
        String[] teamHeaders = {"Team ID", "Name", "Project ID", "Project"};
        String[] teamColumns = {"team_id", "team_name", "project_id", "project_name"};
        
        viewRecords(Query, teamHeaders, teamColumns);
    }
    
    private void getTeamInfo(int id){
        try{
            PreparedStatement search = connectDB().prepareStatement("SELECT tm.team_id, tm.team_name, p.project_name FROM team tm "
                    + "INNER JOIN project p ON tm.project_id = p.project_id "
                    + "WHERE team_id = ?");
            
            search.setInt(1,id);
            
            try (ResultSet result = search.executeQuery()) {
                System.out.println("--------------------------------------------------------------------------------"
                        + "\nSelected team:     | "+result.getString("team_name")
                        + "\n-------------------+"
                        + "\nTeam ID:           | "+result.getInt("team_id")
                        + "\nFrom project:      | "+result.getString("project_name")
                        + "\n--------------------------------------------------------------------------------");
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
    
    private boolean checkSkip(String t_name,int p_id){
        try{
            PreparedStatement findID = connectDB().prepareStatement("SELECT team_id FROM team ORDER BY team_id");
            ResultSet getID = findID.executeQuery();
            
            int previousId = 0;
            
            while (getID.next()) {
                int currentId = getID.getInt("team_id");
                if (previousId != 0 && currentId != previousId + 1) {
                    getID.close();
                    sql = "INSERT INTO team(team_id, team_name, project_id) VALUES (?, ?, ?)";
                    addRecord(sql, (currentId-1), t_name, p_id);
                    return true;
                }
                previousId = currentId;
            }
        } catch(SQLException e){
            System.out.println("Error: "+e.getMessage());
        }
        return false;
    }
}
