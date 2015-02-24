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
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Vance on 1/27/2015.
 */
@WebServlet(name = "GetListingsServlet", urlPatterns = {"/get_listings"})
public class GetListingsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        JSONArray itemArr = new JSONArray();
        PrintWriter out = response.getWriter();
        DBConnect.connect();

        try {
            PreparedStatement st;
            ResultSet rs;
            st = DBConnect.conn.prepareStatement("SELECT LISTING_ID, LISTING_NAME, ENDING_TIME, INITIAL_PRICE FROM LISTING WHERE ENDING_TIME>?");
            st.setTimestamp(1, new Timestamp(new Date().getTime()));
            rs = st.executeQuery();
            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("listingId", rs.getInt("LISTING_ID"));
                item.put("name", rs.getString("LISTING_NAME"));
                //get remaining time by timestamp minus current time
                item.put("endingTime", rs.getTimestamp("ENDING_TIME").getTime() - new Date().getTime());

                //get the highest bidder
                PreparedStatement st2;
                ResultSet rs2;
                st2 = DBConnect.conn.prepareStatement("SELECT USER_NAME, PRICE FROM USERS, BIDDING, LISTING WHERE " +
                        "USER_ID=BIDDER_ID AND BIDDING.LISTING_ID = LISTING.LISTING_ID AND BIDDING.LISTING_ID=? ORDER BY " +
                        "PRICE DESC ");
                //set listing id
                st2.setInt(1, rs.getInt("LISTING_ID"));
                st2.setMaxRows(1);
                rs2 = st2.executeQuery();
                //should have one and only one result; if not then there is no bidder
                if (rs2.next()) {
                    item.put("bidderName", rs2.getString("USER_NAME"));
                    item.put("price", rs2.getString("PRICE"));
                    item.put("hasBidder", true);
                } else {
                    //if no bidder then use the initial price
                    item.put("price", rs.getString("INITIAL_PRICE"));
                    item.put("hasBidder", false);
                }
                st2.close();
                rs2.close();
                itemArr.put(item);
            }

            rs.close();
            st.close();
            json.put("list", itemArr);
            json.put("result", "success");

        } catch (SQLException e) {
            //Logger.getLogger(GetListingsServlet.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }


        DBConnect.disconnect();
        out.print(json.toString());
        out.flush();
    }


}
