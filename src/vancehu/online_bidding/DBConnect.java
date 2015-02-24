package vancehu.online_bidding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Vance on 1/27/2015.
 */

public class DBConnect {

    public static Connection conn;

    //configure derby DB connection info here
    public static void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        String connectionURL = "jdbc:derby://localhost:1527/OnlineBidding";
        try {
            conn = DriverManager.getConnection(connectionURL, "TEST", "TEST");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void disconnect() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
