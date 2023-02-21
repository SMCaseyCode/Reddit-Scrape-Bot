package Data;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bot.CommandManager.keywordMap;
import static Data.ProtectedData.URL;

public class DatabaseManager {

    public static Connection connect() throws SQLException {
        String url = URL.getContent();
        return DriverManager.getConnection(url);
    }

    //Fills keyword data for each server using data stored in SQLite
    public static void fillData(){
        try (Connection conn = connect()){

            String fillQuery = "select * from server order by serverID";
            PreparedStatement ps = conn.prepareStatement(fillQuery);
            ResultSet rs = ps.executeQuery();

            String serverID = "EMPTY ID";
            String keyword;
            List<String> keywordList = new ArrayList<>();

            while (rs.next()){
                //iteration 1, checks for serverID and if keywordList is EMPTY
                if (!Objects.equals(serverID, rs.getString("serverID")) && keywordList.size() == 0){
                    serverID = rs.getString("serverID");
                }
                //iteration > 1 + fills hashMap, checks for serverID and if keywordList is NOT EMPTY
                if (!Objects.equals(serverID, rs.getString("serverID")) && keywordList.size() > 0){
                    keywordMap.put(serverID, keywordList);
                    serverID = rs.getString("serverID");
                    keywordList = new ArrayList<>();
                }
                //Fills list
                if (serverID.equals(rs.getString("serverID"))){
                    keyword = rs.getString("keyword");
                    keywordList.add(keyword);
                }
            }

            // Commits final map. Needed because rs.next() cuts out the last iteration.
            keywordMap.put(serverID, keywordList);
            System.out.println("Keywords loaded successfully!");
            ps.close();

        } catch (SQLException e){
            System.out.println("FILLDATA SQL ERROR: " + e);
        }
    }

    //Inserts keyword into DB
    public void insertKeyword (String serverID, String keyword) {
        try (Connection conn = connect()){

            String insertQuery = "insert into server(serverID, keyword) values(?,?)";
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            ps.setString(1, serverID);
            ps.setString(2, keyword);

            ps.executeUpdate();
            ps.close();

        } catch (SQLException e){
            System.out.println("INSERT SQL ERROR: " + e);
        }
    }

    //Deletes a keyword out of the active list for a server
    public void deleteKeyword (String serverID, String keyword) {
        try (Connection conn = connect()) {

            String deleteQuery = "delete from server where keyword =? and serverID=?";
            PreparedStatement ps = conn.prepareStatement(deleteQuery);

            ps.setString(1, keyword);
            ps.setString(2, serverID);

            ps.executeUpdate();
            ps.close();

        } catch (SQLException e){
            System.out.println("DELETE SQL ERROR: " + e);
        }
    }

    //Wipes all keywords for a server
    public void wipeKeywords (String serverID) {
        try (Connection conn = connect()) {

            String wipeQuery = "delete from server where serverID=?";
            PreparedStatement ps = conn.prepareStatement(wipeQuery);

            ps.setString(1, serverID);
            ps.executeUpdate();
            ps.close();

        } catch (SQLException e){
            System.out.println("WIPE SQL ERROR: " + e);
        }
    }

    //Sets desired channel for a server
    public void setChannel(String serverID, String channelID){
        try(Connection conn = connect()){

            String channelQuery = "insert into channel(serverID, channelID) values(?,?)";
            PreparedStatement ps = conn.prepareStatement(channelQuery);

            ps.setString(1, serverID);
            ps.setString(2, channelID);
            ps.executeUpdate();
            ps.close();

        }catch (SQLException e){
            System.out.println("SETCHANNEL SQL ERROR: " + e);
        }
    }

    //Removes desired channel
    public void delChannel(String serverID){
        try(Connection conn = connect()){

            String channelQuery = "delete from channel where serverID =?";
            PreparedStatement ps = conn.prepareStatement(channelQuery);

            ps.setString(1, serverID);
            ps.executeUpdate();
            ps.close();

        }catch (SQLException e){
            System.out.println("DELCHANNEL SQL ERROR: " + e);
        }
    }

    //Checks for already entered servers for setchannel
    public boolean dupeCheck(String serverID){
        try(Connection conn = connect()){

            String dupeQuery = "select serverID from channel";
            PreparedStatement ps = conn.prepareStatement(dupeQuery);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                if (Objects.equals(rs.getString("serverID"), serverID)){
                    return true;
                }
            }
            ps.close();

        }catch (SQLException e){
            System.out.println("DUPECHECK SQL ERROR: " + e);
        }

        return false;
    }

}
