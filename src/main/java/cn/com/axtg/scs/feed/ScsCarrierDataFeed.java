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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseStatusExample;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsCarrierDataFeed.java
 *
 * Creation Date: Sep 12, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsCarrierDataFeed extends HttpServlet{

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String parentCarrier = request.getParameter("parentcarrier");
        
        ScseStatusExample exa = new ScseStatusExample();
        exa.createCriteria().andParentCarrierEqualTo(new Long(parentCarrier));
        
        List<ScseStatus> statuses = ScseInitServlet.getStatusDao().selectByExample(exa);
        
        JSONArray ja = new JSONArray();
        
        try {
            String output = "";
            for(ScseStatus status:statuses) {
                JSONObject json = new JSONObject();
                String s = status.getDisplayValue();
                Long id = status.getPkId();
                
                json.put("id", id);
                json.put("value", s);
                
                ja.add(json);
//                ja.add
            }
            
            out.println(ja);
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
