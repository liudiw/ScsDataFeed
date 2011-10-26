/**
 * 
 */
package cn.com.axtg.scs.feed;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.com.axtg.scsemod.model.ScseConsignment;
import cn.com.axtg.scsemod.model.ScseConsignmentExample;
import cn.com.axtg.scsemod.model.ScsePickupJob;
import cn.com.axtg.scsemod.model.ScsePickupJobExample;
import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseStatusExample;
import cn.com.axtg.scsemod.model.ScseUser;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsNewJobTicket.java
 *
 * Creation Date: Sep 25, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsNewJobTicket extends HttpServlet{

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String userId = request.getParameter("userid");
        String amount = request.getParameter("amount");
        String status = request.getParameter("status");
        
        Long userPkId = Long.parseLong(userId);
        
        int amt = 1;
        try{
        	amt = Integer.parseInt(amount);
        } catch(Exception ex) {}
        
        ScseUser user = ScseInitServlet.getUserDAO().selectByPrimaryKey(userPkId);
        
        long parentCarrier = user.getParentCarrier();
        
        ScseStatus startStatus = getConnoteStartStatus(parentCarrier);
        
        List<ScseConsignment> consignments = getNewConsignments(user.getParentDepot(), startStatus.getPkId(), parentCarrier);
        
        JSONArray ja = new JSONArray();
        
        
        try {
//            String output = "";
//            int count = 0;
//            for(ScseConsignment consignment:consignments) {
//            	if(count >= amt) break;
//            	
//                JSONObject json = new JSONObject();
//                
//                json.put("pk_consignment_id", consignment.getPkId());
//                json.put("connote_number", consignment.getConnoteNumber());
//                json.put("last_updated", new SimpleDateFormat("yyyy-MM-dd").format(consignment.getLastUpdated()));
//                json.put("sender_name", consignment.getSenderName());
//                json.put("pickup_contact_number", consignment.getPickupContactNumber());
//                json.put("pickup_address", consignment.getPickupAddress());
//                json.put("pickup_area", consignment.getPickupArea());
//                json.put("pickup_suburb", consignment.getPickupSuburb());
//                json.put("pickup_state", consignment.getPickupState());
//                json.put("pickup_country", consignment.getPickupCountry());
//                json.put("pickup_postcode", consignment.getPickupPostcode());
//                json.put("pickup_time", consignment.getPickupTime());
//                
//                ja.add(json);
//                
//                count ++;
////                ja.add
//            }
            ja = getPickupJobString(userId, amount, status);
            out.println(ja);
        } catch(Exception ex) {
        	ex.printStackTrace();
        }finally {            
            out.close();
        }
	}
	
	private JSONArray getPickupJobString(String userId, String amount, String status) {
        Long userPkId = Long.parseLong(userId);
        
        int amt = 1;
        try{
        	amt = Integer.parseInt(amount);
        } catch(Exception ex) {}
        
        ScseUser user = ScseInitServlet.getUserDAO().selectByPrimaryKey(userPkId);
        
//        ScsePickupJobExample exa = new ScsePickupJobExample();
//        if(status != null && !status.equals("")) {
//	        exa.createCriteria().andParentCarrierEqualTo(user.getParentCarrier()).
//	        	andParentDepotEqualTo(user.getParentDepot()).andJobStatusEqualTo(status).
//	        	andExifParentUserEqualTo(Long.parseLong(userId));
//        } else {
//	        exa.createCriteria().andParentCarrierEqualTo(user.getParentCarrier()).
//        	andParentDepotEqualTo(user.getParentDepot());
//        }
        
        List<ScsePickupJob> jobs = new ArrayList<ScsePickupJob>();
        List<ScsePickupJob> newJobs = getNewPickupJob(user);
        List<ScsePickupJob> assignedJobs = getAssignedPickupJob(user);
        jobs.addAll(assignedJobs);
        int countNewJob = 0;
        for(ScsePickupJob job:newJobs) {
        	if(countNewJob >= amt) break;
        	jobs.add(job);
        	countNewJob ++;
        }
        
        JSONArray ja = new JSONArray();
        
        
        try {
            String output = "";
            int count = 0;
            for(ScsePickupJob job:jobs) {
//            	if(count >= amt) break;
            	
                JSONObject json = new JSONObject();
//                public static final String PICKUP_JOB_ROW_ID = "_id";
//                public static final String PICKUP_JOB_PK_ID = "pk_pickup_job_id";
//                public static final String PICKUP_JOB_LAST_UPDATED = "last_updated";
//                public static final String PICKUP_JOB_PICKUP_NAME = "pickup_name";
//                public static final String PICKUP_JOB_PICKUP_COMPANY = "pickup_company";
//                public static final String PICKUP_JOB_PICKUP_ADDRESS = "pickup_address";
//                public static final String PICKUP_JOB_PICKUP_AREA = "pickup_area";
//                public static final String PICKUP_JOB_PICKUP_SUBURB = "pickup_suburb";
//                public static final String PICKUP_JOB_PICKUP_STATE = "pickup_state";
//                public static final String PICKUP_JOB_PICKUP_COUNTRY = "pickup_country";
//                public static final String PICKUP_JOB_PICKUP_POSTCODE = "pickup_postcode";
//                public static final String PICKUP_JOB_PICKUP_CONTACT_NUMBER = "pickup_contact_number";
//                public static final String PICKUP_JOB_JOB_TYPE = "job_type";
//                public static final String PICKUP_JOB_JOB_STATUS = "job_status";
//                public static final String PICKUP_JOB_PICKUP_TIME = "pickup_time";
//                public static final String PICKUP_JOB_PICKUP_NOTES = "notes";
//                public static final String PICKUP_JOB_PICKUP_CONNOTE_NUMBER = "connote_number";
//                public static final String PICKUP_JOB_PARENT_CARRIER = "parent_carrier";
//                public static final String PICKUP_JOB_PARENT_DEPOT = "parent_depot";
                
                json.put("pk_pickup_job_id", job.getPkId());
                json.put("last_updated", new SimpleDateFormat("yyyy-MM-dd").format(job.getLastUpdated()));
                json.put("pickup_name", job.getPickupName());
                json.put("pickup_company", job.getPickupCompany());
                json.put("pickup_address", job.getPickupAddress());
                json.put("pickup_area", job.getPickupArea());
                json.put("pickup_suburb", job.getPickupSuburb());
                json.put("pickup_state", job.getPickupState());
                json.put("pickup_country", job.getPickupCountry());
                json.put("pickup_postcode", job.getPickupPostcode());
                json.put("pickup_contact_number", job.getPickupContactNumber());
                json.put("job_type", job.getJobType());
                json.put("job_status", job.getJobStatus());
                json.put("pickup_time", job.getPickupTime());
                json.put("notes", job.getNotes());
                long parentConsignment = job.getParentConsignment();
                
                if(parentConsignment == 0) {
                	json.put("connote_number", "无关联运单");
                } else {
                	try{
	                	ScseConsignment con = ScseInitServlet.getConsignmentDao().selectByPrimaryKey(parentConsignment);
	                	json.put("connote_number", con.getConnoteNumber());
                	} catch(Exception exce) {
                		exce.printStackTrace();
                	}
                }
                
                json.put("parent_carrier", job.getParentCarrier());
                json.put("parent_depot", job.getParentDepot());
                
                ja.add(json);
                
                count ++;
//                ja.add
            }
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
        
        System.out.println("================================ JSON String: " + ja.toString());
            
        return ja;
	}
	
	private List<ScsePickupJob> getNewPickupJob(ScseUser user) {
		ScsePickupJobExample exa = new ScsePickupJobExample();
        exa.createCriteria().andParentCarrierEqualTo(user.getParentCarrier()).
	    	andParentDepotEqualTo(user.getParentDepot()).
	    	andJobStatusEqualTo("CREATED").
	    	andExifParentUserEqualTo(0l);
        List<ScsePickupJob> jobs = ScseInitServlet.getPickupJobDao().selectByExample(exa);
        
        return jobs;
	}
	
	private List<ScsePickupJob> getAssignedPickupJob(ScseUser user) {
		ScsePickupJobExample exa = new ScsePickupJobExample();
        exa.createCriteria().andParentCarrierEqualTo(user.getParentCarrier()).
	    	andParentDepotEqualTo(user.getParentDepot()).
	    	andJobStatusEqualTo("ASSIGNED").
	    	andExifParentUserEqualTo(user.getPkId());
        List<ScsePickupJob> jobs = ScseInitServlet.getPickupJobDao().selectByExample(exa);
        
        return jobs;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private ScseStatus getConnoteStartStatus(long parentCarrier) {
		ScseStatusExample exa = new ScseStatusExample();
		exa.createCriteria().andParentCarrierEqualTo(parentCarrier).andIsStartEqualTo(true);
		List<ScseStatus> statuses = ScseInitServlet.getStatusDao().selectByExample(exa);
		
		if(statuses.size() > 0) {
			return statuses.get(0);
		}
		
		return null;
	}
	
	private List<ScseConsignment> getNewConsignments(long parentDepot, long parentStatus, long parentCarrier) {
		ScseConsignmentExample exa = new ScseConsignmentExample();
		exa.createCriteria().andParentCarrierEqualTo(parentCarrier)
			.andExifCurrentStatusEqualTo(parentStatus)
			.andExifStartDepotEqualTo(parentDepot);
		
		
		List<ScseConsignment> consignments = ScseInitServlet.getConsignmentDao().selectByExampleWithBLOBs(exa);
		
		return consignments;
	}
	
	/*** test ***/
	public static void main(String args[]) {
		try{
			ScseUser user = ScseInitServlet.getUserDAO().selectByPrimaryKey(15l);
			ScsePickupJobExample exa = new ScsePickupJobExample();
	        exa.createCriteria().andParentCarrierEqualTo(user.getParentCarrier()).
		    	andParentDepotEqualTo(user.getParentDepot()).
		    	andJobStatusEqualTo("CREATED").
		    	andExifParentUserEqualTo(0l);
	        List<ScsePickupJob> jobs = ScseInitServlet.getPickupJobDao().selectByExample(exa);
	        
	        for(ScsePickupJob job:jobs) {
	        	System.out.println(job.getPickupAddress() + ", " + job.getPickupName());
	        }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
