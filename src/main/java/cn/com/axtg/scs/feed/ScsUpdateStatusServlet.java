/**
 * 
 */
package cn.com.axtg.scs.feed;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.axtg.scsemod.model.ScseConsignment;
import cn.com.axtg.scsemod.model.ScseConsignmentExample;
import cn.com.axtg.scsemod.model.ScseConsignmentStatus;
import cn.com.axtg.scsemod.model.ScseConsignmentStatusExample;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsUpdateStatusServlet.java
 *
 * Creation Date: Sep 15, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsUpdateStatusServlet extends HttpServlet{

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String connoteNumber = request.getParameter("connotenumber");
        String statusId = request.getParameter("statusid");
        String depotId = request.getParameter("depotid");
        String userId = request.getParameter("userid");
        String cod = request.getParameter("cod");
        		
        
        ScseConsignmentExample exa = new ScseConsignmentExample();
        exa.createCriteria().andConnoteNumberEqualTo(connoteNumber);
        
        try {
        
	        List<ScseConsignment> connotes = ScseInitServlet.getConsignmentDao().selectByExampleWithoutBLOBs(exa);
	        
	        if(connotes.size() == 1) {
	        	ScseConsignment connote = connotes.get(0);
	        	connote.setExifCurrentDepot(new Long(depotId));
	        	connote.setExifCurrentStatus(new Long(statusId));
	        	connote.setCodCollected(("1".equals(cod)?true:false));
	        	if(connote.isCodCollected()) {
	        		connote.setCodCollectedBy(Long.parseLong(userId));
	        	}
	        	
	        	ScseConsignmentStatusExample sExa = new ScseConsignmentStatusExample();
	        	sExa.createCriteria().andParentConsignmentEqualTo(connote.getPkId()).andIsCurrentEqualTo(true);
	        	
	        	List<ScseConsignmentStatus> statuses = ScseInitServlet.getConsignmentStatusDao().selectByExample(sExa);
	        	
	        	for(ScseConsignmentStatus status:statuses) {
	        		try{
	        			status.setIsCurrent(false);
	        			ScseInitServlet.getConsignmentStatusDao().updateByPrimaryKey(status);
	        		} catch(Exception exc) {
	        			exc.printStackTrace();
	        		}
	        	}
	        	
	        	ScseConsignmentStatus st = new ScseConsignmentStatus();
	        	st.setParentConsignment(connote.getPkId());
	        	st.setParentDepot(new Long(depotId));
	        	st.setParentStatus(new Long(statusId));
	        	st.setIsCurrent(true);
	        	st.setParentUser(new Long(userId));
	        	
	        	ScseInitServlet.getConsignmentDao().updateByPrimaryKeyWithoutBLOBs(connote);
	        	ScseInitServlet.getConsignmentStatusDao().insert(st);
	            
	        	out.println("面单 " + connoteNumber + "状态已升级！");
	        } else {
	        	out.println("状态升级发生错误");
	        }
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
