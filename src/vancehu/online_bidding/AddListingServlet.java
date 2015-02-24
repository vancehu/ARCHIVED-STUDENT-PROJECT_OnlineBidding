package vancehu.online_bidding;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;

/**
 * Created by Vance on 1/27/2015.
 */
@WebServlet(name = "AddListingServlet", urlPatterns = {"/add_listing"})
public class AddListingServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        PrintWriter out = response.getWriter();
        Map<String, String[]> params = request.getParameterMap();
        DBConnect.connect();

        try {
            PreparedStatement st;
            st = DBConnect.conn.prepareStatement("INSERT INTO LISTING (LISTING_NAME, DESCRIPTION, CREATOR_ID, " +
                    "INITIAL_PRICE, ENDING_TIME) VALUES (?,?,?,?,?)");

            /*for required values, use Map.get() directly and throw null and invalid format exceptions;
             for optional values, add if else statement using Map.containsKey() before accessing the values.
              */

            //listing_name[r]
            st.setString(1, params.get("listing_name")[0]);

            //description[o]
            if (params.containsKey("description")) {
                st.setString(2, params.get("description")[0]);
            } else {
                st.setNull(2, Types.VARCHAR);
            }

            //creator_id - session id
            st.setString(3, request.getSession().getId());

            //initial_price[r]
            st.setBigDecimal(4, new BigDecimal(params.get("initial_price")[0]));

            //timer[r]: ending_time = current timestamp + timer
            long timer = Long.parseLong(params.get("timer")[0]);
            st.setTimestamp(5, new Timestamp(new java.util.Date().getTime() + timer));

            st.executeUpdate();
            st.close();

            json.put("result", "success");

        } catch (NullPointerException | NumberFormatException e) {
            //return error message without logging
            json.put("result", "error");
        } catch (SQLException e) {
            //other unexpected sql exceptions
            //Logger.getLogger(AddListingServlet.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }

        DBConnect.disconnect();
        out.print(json.toString());
        out.flush();

    }
}
