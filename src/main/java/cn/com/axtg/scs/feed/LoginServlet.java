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

import org.json.simple.JSONObject;

import cn.com.axtg.scs.feed.util.MD5;
import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseUser;
import cn.com.axtg.scsemod.model.ScseUserExample;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: LoginServlet.java
 *
 * Creation Date: Sep 14, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class LoginServlet extends HttpServlet{

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");        
        
        String encrypted = MD5.encrypt(password);
        
        ScseUserExample exa = new ScseUserExample();
        exa.createCriteria().andUsernameEqualTo(username).andPasswordEqualTo(encrypted).andParentCarrierNotEqualTo(0l);
        
        List<ScseUser> users = ScseInitServlet.getUserDAO().selectByExample(exa);
        
        String output = "";
		JSONObject json = new JSONObject();
        try {
        	
        	if(users.size() == 1) {
        		ScseUser user = users.get(0);
        		Long pkId = user.getPkId();
        		String firstname = user.getFirstname();
        		String lastname = user.getLastname();
        		Long parentCarrier = user.getParentCarrier();
        		Long parentDepot = user.getParentDepot();
        		
        		json.put("code", "SUCCESSFUL");
        		json.put("pk_id", pkId);
        		json.put("firstname", firstname);
        		json.put("lastname", lastname);
        		json.put("parent_carrier", parentCarrier);
        		json.put("parent_depot", parentDepot);
        		
        	} else {
        		json.put("code", "FAILURE");
        	}
            
            out.println(json);
        }catch(Exception ex){
        	ex.printStackTrace();
        } finally {            
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
