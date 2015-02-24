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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Vance on 1/27/2015.
 * Return user name by user session id
 */
@WebServlet(name = "GetUserServlet", urlPatterns = {"/get_user"})
public class GetUserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        PrintWriter out = response.getWriter();
        DBConnect.connect();

        try {
            PreparedStatement st;
            ResultSet rs;
            st = DBConnect.conn.prepareStatement("SELECT * FROM USERS WHERE USER_ID=?");

            /*for required values, use Map.get() directly and throw null and invalid format exceptions;
             for optional values, add if else statement using Map.containsKey() before accessing the values.
              */

            st.setString(1, request.getSession().getId());
            rs = st.executeQuery();
            if (rs.next()) {
                json.put("userName", rs.getString("USER_NAME"));
                json.put("result", "success");
            } else {
                json.put("result", "nofind");
            }
            rs.close();
            st.close();

        } catch (SQLException e) {
            //other unexpected sql exceptions
            //Logger.getLogger(GetUserServlet.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }

        DBConnect.disconnect();
        out.print(json);
        out.flush();
    }
}
