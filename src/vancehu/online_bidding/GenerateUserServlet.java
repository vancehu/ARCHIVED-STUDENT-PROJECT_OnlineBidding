package vancehu.online_bidding;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Created by Vance on 1/27/2015.
 * Generate a new user once requested;
 * use the last digits of sid as the default name.
 */

@WebServlet(name = "GenerateUserServlet", urlPatterns = {"/gen_user"})
public class GenerateUserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        PrintWriter out = response.getWriter();
        String sid = "";
        String newUserName = "";
        DBConnect.connect();

        //insert new username to the database
        while (true) {
            try {
                //generate a new session
                request.getSession().invalidate();
                sid = request.getSession().getId();
                PreparedStatement st;

                //use last 8 digits of session ID as default user name
                newUserName = "User_" + sid.substring(sid.length() - 8);
                st = DBConnect.conn.prepareStatement("INSERT INTO USERS (USER_ID, USER_NAME) VALUES (?,?)");
                st.setString(1, sid);
                st.setString(2, newUserName);
                st.executeUpdate();
                st.close();

                json.put("userName", newUserName);
                json.put("result", "success");
                break;
            } catch (SQLIntegrityConstraintViolationException e) {
                //loop when violates unique constraint (generate a new name)
            } catch (SQLException e) {
                //Logger.getLogger(GenerateUserServlet.class.getName()).log(Level.SEVERE, null, e);
                json.put("result", "error");
                break;
            }
        }
        DBConnect.disconnect();
        out.print(json);
        out.flush();

    }

}