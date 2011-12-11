/**
 * 
 */
package cn.com.axtg.scs.feed;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import cn.com.axtg.scsemod.model.ScseConsignment;
import cn.com.axtg.scsemod.model.ScseConsignmentExample;
import cn.com.axtg.scsemod.model.ScseConsignmentItem;
import cn.com.axtg.scsemod.model.ScseConsignmentItemExample;
import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseStatusExample;
import cn.com.axtg.scsemod.model.ScseUser;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsCodInfoServlet.java
 *
 * Creation Date: Dec 11, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsCodInfoServlet extends HttpServlet{


	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String connotenumber = request.getParameter("connotenumber");
        String userId = request.getParameter("userid");
        String statusId = request.getParameter("statusid");
        
        Long userPkId = Long.parseLong(userId);
        Long statusPkId = Long.parseLong(statusId);
        
        try {
	        ScseUser user = ScseInitServlet.getUserDAO().selectByPrimaryKey(userPkId);
	        
	        long parentCarrier = user.getParentCarrier();

        	ScseConsignmentExample exa = new ScseConsignmentExample();
        	exa.createCriteria().andParentCarrierEqualTo(parentCarrier).andConnoteNumberEqualTo(connotenumber);
        	List<ScseConsignment> connotes = ScseInitServlet.getConsignmentDao().selectByExampleWithBLOBs(exa);
        	double totalPrice = 0d;
        	for(ScseConsignment connote:connotes) {
        		ScseConsignmentItemExample itemExa = new ScseConsignmentItemExample();
        		itemExa.createCriteria().andParentConsignmentEqualTo(connote.getPkId());
        		List<ScseConsignmentItem> items = ScseInitServlet.getConsignmentItemDao().selectByExample(itemExa);
        		for(ScseConsignmentItem item:items) {
        			try{
	        			int qty = item.getQuantity();
	        			double price = item.getPrice();
	        			totalPrice += qty * price;
        			} catch(Exception ex) {
        				ex.printStackTrace();
        			}
        		}
        	}
        	
        	// check status to determine whether to collect the cash. only collect at delivery
    		ScseStatus status = ScseInitServlet.getStatusDao().selectByPrimaryKey(statusPkId);
    		boolean isEnd = status.isIsEnd();
    		boolean isProblem = status.isIsProblem();
    		
    		if(isProblem) {
    			totalPrice = 0;
    		}
    		
    		if(!isEnd) {
    			totalPrice = 0;
    		}
	        
	        JSONObject json = new JSONObject();
	        json.put("total_price", new DecimalFormat("0.00").format(totalPrice));
            
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
}
