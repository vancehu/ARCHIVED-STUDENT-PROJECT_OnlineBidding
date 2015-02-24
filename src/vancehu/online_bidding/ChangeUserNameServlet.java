package vancehu.online_bidding;

import org.json.JSONArray;
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
import java.util.Map;

/**
 * Created by Vance on 1/27/2015.
 */
@WebServlet(name = "ChangeUserNameServlet", urlPatterns = {"/chg_name"})
public class ChangeUserNameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        PrintWriter out = response.getWriter();
        Map<String, String[]> params = request.getParameterMap();
        DBConnect.connect();

        try {
            PreparedStatement st;
            st = DBConnect.conn.prepareStatement("UPDATE USERS SET USER_NAME = ? WHERE USER_ID = ?");

            /*for required values, use Map.get() directly and throw null and invalid format exceptions;
             for optional values, add if else statement using Map.containsKey() before accessing the values.
              */

            //required
            st.setString(1, params.get("user_name")[0]);
            st.setString(2, request.getSession().getId());

            if (st.executeUpdate() == 1) {
                //only one row is affected should be correct.
                json.put("result", "success");
            } else {
                //no row affected; something goes wrong.
                json.put("result", "error");
            }
            st.close();

        } catch (NullPointerException | NumberFormatException e) {
            //return error message without logging
            json.put("result", "error");
        } catch (SQLIntegrityConstraintViolationException e) {
            //let the client side know there is a same name existing
            json.put("result", "duplicate");
        } catch (SQLException e) {
            //other unexpected sql exceptions
            //Logger.getLogger(ChangeUserName.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }

        DBConnect.disconnect();
        out.print(json);
        out.flush();
    }
}
