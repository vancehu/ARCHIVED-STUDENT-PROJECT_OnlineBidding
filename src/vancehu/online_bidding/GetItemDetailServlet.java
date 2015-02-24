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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Created by Vance on 1/27/2015.
 */
@WebServlet(name = "GetItemDetailServlet", urlPatterns = {"/get_detail"})
public class GetItemDetailServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        JSONArray bidArr = new JSONArray();
        PrintWriter out = response.getWriter();
        Map<String, String[]> params = request.getParameterMap();
        DBConnect.connect();

        try {
            PreparedStatement st;
            ResultSet rs;
            st = DBConnect.conn.prepareStatement("SELECT * FROM LISTING WHERE LISTING_ID = ?");

            /*for required values, use Map.get() directly and throw null and invalid format exceptions;
             for optional values, add if else statement using Map.containsKey() before accessing the values.
              */

            //listing id[r]
            st.setInt(1, Integer.parseInt(params.get("LISTING_ID")[0]));
            rs = st.executeQuery();
            if (rs.next()) {
                json.put("name", rs.getString("LISTING_NAME"));
                json.put("description", rs.getString("DESCRIPTION"));
                //get remaining time by timestamp minus current time
                json.put("endingTime", rs.getTimestamp("ENDING_TIME").getTime() - new Date().getTime());
                json.put("initialPrice", rs.getString("INITIAL_PRICE"));
                json.put("creatorId", rs.getString("CREATOR_ID"));
                //get the list of bids
                PreparedStatement st2;
                ResultSet rs2;
                st2 = DBConnect.conn.prepareStatement("SELECT USER_NAME, PRICE FROM USERS, BIDDING, LISTING WHERE USER_ID " +
                        "= BIDDER_ID AND BIDDING.LISTING_ID = LISTING.LISTING_ID ORDER BY PRICE DESC ");
                rs2 = st2.executeQuery();

                while (rs2.next()) {
                    JSONObject bid = new JSONObject();
                    bid.put("bidderName", rs2.getString("USER_NAME"));
                    bid.put("price", rs2.getString("PRICE"));
                    bidArr.put(bid);
                }
                if (bidArr.length() > 0) {
                    json.put("bids", bidArr);
                    json.put("hasBidder", true);
                } else {
                    json.put("hasBidder", false);
                }
                rs2.close();
                st2.close();
                rs.close();
                st.close();
                json.put("result", "success");
            } else {
                //the listing doesn't exist
                json.put("result", "error");
            }


        } catch (NullPointerException | NumberFormatException e) {
            //return error message without logging
            json.put("result", "error");
        } catch (SQLException e) {
            //other unexpected sql exceptions
            //Logger.getLogger(GetItemDetailServlet.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }

        DBConnect.disconnect();
        out.print(json);
        out.flush();

    }
}
