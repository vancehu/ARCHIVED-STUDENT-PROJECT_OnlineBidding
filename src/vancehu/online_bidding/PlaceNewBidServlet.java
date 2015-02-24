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
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Vance on 1/27/2015.
 */
@WebServlet(name = "PlaceNewBidServlet", urlPatterns = {"/place_new"})
public class PlaceNewBidServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject json = new JSONObject();
        JSONArray itemArr = new JSONArray();
        PrintWriter out = response.getWriter();
        Map<String, String[]> params = request.getParameterMap();
        DBConnect.connect();
        try {
            //get the highest bidder
            PreparedStatement st;
            ResultSet rs;
            st = DBConnect.conn.prepareStatement("SELECT USER_ID, PRICE FROM USERS, BIDDING, LISTING WHERE " +
                    "USER_ID=BIDDER_ID AND BIDDING.LISTING_ID = LISTING.LISTING_ID AND BIDDING.LISTING_ID=? AND ENDING_TIME>? ORDER BY " +
                    "PRICE DESC ");

            /*for required values, use Map.get() directly and throw null and invalid format exceptions;
             for optional values, add if else statement using Map.containsKey() before accessing the values.
              */

            //listing id [r]
            int lid = Integer.parseInt(params.get("LISTING_ID")[0]);
            st.setInt(1, lid);
            //ending time > current time
            st.setTimestamp(2, new Timestamp(new Date().getTime()));
            BigDecimal newPrice = new BigDecimal(params.get("PRICE")[0]);
            st.setMaxRows(1);
            rs = st.executeQuery();

            if (rs.next()) {
                //have bids
                if (rs.getString("USER_ID").equals(request.getSession().getId())) {
                    //current session is the highest bidder
                    json.put("result", "alreadyHighest");
                } else if (rs.getBigDecimal("PRICE").compareTo(newPrice) >= 0) {
                    //the current price offer is not greater than the highest price
                    json.put("result", "invalidPrice");
                } else {
                    //qualified, insert as the highest
                    PreparedStatement st2;
                    st2 = DBConnect.conn.prepareStatement("INSERT INTO BIDDING (LISTING_ID, BIDDER_ID, EVENT_TIME, PRICE) VALUES (?, ?, ?, ?)");
                    st2.setInt(1, lid);
                    st2.setString(2, request.getSession().getId());
                    st2.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
                    st2.setBigDecimal(4, newPrice);
                    st2.executeUpdate();
                    st2.close();
                    json.put("result", "success");
                }
            } else {
                //no bids, check the initial price
                PreparedStatement st2;
                ResultSet rs2;
                st2 = DBConnect.conn.prepareStatement("SELECT INITIAL_PRICE FROM LISTING WHERE LISTING_ID=? AND ENDING_TIME>?");
                st2.setInt(1, lid);
                st2.setTimestamp(2, new Timestamp(new Date().getTime()));
                rs2 = st2.executeQuery();
                if (rs2.next()) {
                    //the listing exists
                    if (rs2.getBigDecimal("INITIAL_PRICE").compareTo(newPrice) >= 0) {
                        //the current price offer is not greater than the initial price
                        json.put("result", "invalidPrice");
                    } else {
                        //qualified, insert as the highest
                        PreparedStatement st3;
                        st3 = DBConnect.conn.prepareStatement("INSERT INTO BIDDING (LISTING_ID, BIDDER_ID, EVENT_TIME, PRICE) VALUES (?, ?, ?, ?)");
                        st3.setInt(1, lid);
                        st3.setString(2, request.getSession().getId());
                        st3.setTimestamp(3, new Timestamp(new Date().getTime()));
                        st3.setBigDecimal(4, newPrice);
                        st3.executeUpdate();
                        st3.close();
                        json.put("result", "success");
                    }
                } else {
                    //no listing found, return error
                    json.put("result", "error");
                }
                rs2.close();
                st2.close();
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            //other unexpected sql exceptions
            Logger.getLogger(PlaceNewBidServlet.class.getName()).log(Level.SEVERE, null, e);
            json.put("result", "error");
        }

        DBConnect.disconnect();
        out.print(json.toString());
        out.flush();
    }
}
