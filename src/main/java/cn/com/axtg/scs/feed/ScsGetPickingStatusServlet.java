/**
 * 
 */
package cn.com.axtg.scs.feed;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.com.axtg.scsemod.model.ScseConsignment;
import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseStatusExample;
import cn.com.axtg.scsemod.model.ScseUser;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsConfirmPickupServlet.java
 *
 * Creation Date: Sep 26, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsGetPickingStatusServlet extends HttpServlet{


	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String userId = request.getParameter("userid");
        
        Long userPkId = Long.parseLong(userId);
        
        try {
	        ScseUser user = ScseInitServlet.getUserDAO().selectByPrimaryKey(userPkId);
	        
	        long parentCarrier = user.getParentCarrier();
	        
	        ScseStatus picking = getPickingUpStatus(parentCarrier);
	        
	        JSONObject json = new JSONObject();
	        json.put("status_name", picking.getName());
	        json.put("pk_stataus_id", picking.getPkId());
	        json.put("description", picking.getDescription());
	        json.put("display_value", picking.getDisplayValue());
            
            out.println(json);
        } catch(Exception ex) {
        	ex.printStackTrace();
        }finally {            
            out.close();
        }
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private ScseStatus getPickingUpStatus(long parentCarrier) {
		ScseStatusExample exa = new ScseStatusExample();
		exa.createCriteria().andParentCarrierEqualTo(parentCarrier).andIsPickingEqualTo(true);
		List<ScseStatus> statuses = ScseInitServlet.getStatusDao().selectByExample(exa);
		
		if(statuses.size() > 0) {
			return statuses.get(0);
		}
		
		return null;
	}
}
