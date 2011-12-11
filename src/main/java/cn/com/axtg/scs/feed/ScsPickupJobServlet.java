/**
 * 
 */
package cn.com.axtg.scs.feed;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.axtg.scsemod.model.ScseConsignment;
import cn.com.axtg.scsemod.model.ScseConsignmentExample;
import cn.com.axtg.scsemod.model.ScseConsignmentStatus;
import cn.com.axtg.scsemod.model.ScseConsignmentStatusExample;
import cn.com.axtg.scsemod.model.ScsePickupJob;
import cn.com.axtg.scsemod.model.ScsePickupJobGps;
import cn.com.axtg.scsemod.model.ScseStatus;
import cn.com.axtg.scsemod.model.ScseStatusExample;
import cn.com.axtg.scsemod.model.ScseUserPickupJob;
import cn.com.axtg.scsemod.model.ScseUserPickupJobExample;
import cn.com.axtg.scsemod.server.ScseInitServlet;

/**
 * Project: ScsDataFeed
 *
 * Filename: ScsPickupJobServlet.java
 *
 * Creation Date: Oct 17, 2011
 *
 * @author AXTG
 *
 * Copyright (c) 2011 奥信拓高
 */
public class ScsPickupJobServlet extends HttpServlet {

	public static final int PICKUP_JOB_OPERATION_HOLD = 1;

	public static final int PICKUP_JOB_OPERATION_RELEASE = 2;

	public static final int PICKUP_JOB_OPERATION_DONE = 3;

	public static final int PICKUP_JOB_OPERATION_GPS = 4;
	
	private static final String CONNOTE_PLACE_HOLDER = "-";

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html;charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    
	    String userId = request.getParameter("userid");
	    String pickupJobId = request.getParameter("pickupjobid");
	    String oper = request.getParameter("operation");
	    String connoteNumber = request.getParameter("connotenumber");
	    String parentCarrier = request.getParameter("parentcarrier");
	    String parentDepot = request.getParameter("parentdepot");
	    
	    
	    Long userPkId = Long.parseLong(userId);
	    int operation = Integer.parseInt(oper);
    	long pickupJobPkId = Long.parseLong(pickupJobId);
	    
	    switch(operation) {
	    case PICKUP_JOB_OPERATION_HOLD:
	    	holdPickupJob(pickupJobPkId, userPkId);
	    	break;
	    case PICKUP_JOB_OPERATION_RELEASE:
	    	releaseJob(pickupJobPkId, userPkId);
	    	break;
	    case PICKUP_JOB_OPERATION_GPS:
	    	try{
		    	String lat = request.getParameter("lat");
		    	String lng = request.getParameter("lng");
		    	ScsePickupJobGps gps = new ScsePickupJobGps();
		    	gps.setParentPickupJob(Long.parseLong(pickupJobId));
		    	gps.setLatitude(Float.parseFloat(lat));
		    	gps.setLongitude(Float.parseFloat(lng));
		    	
		    	ScseInitServlet.getPickupJobGpsDao().insert(gps);
	    	} catch(Exception exce) {
	    		exce.printStackTrace();
	    	}
	    	break;
	    case PICKUP_JOB_OPERATION_DONE:
	    	String pickupName = URLDecoder.decode(request.getParameter("pickupname"), "UTF-8");
	    	String pickupCompany = URLDecoder.decode(request.getParameter("pickupcompany"), "UTF-8");
	    	String pickupAddress = URLDecoder.decode(request.getParameter("pickupaddress"), "UTF-8");
	    	String pickupArea = URLDecoder.decode(request.getParameter("pickuparea"), "UTF-8");
	    	String pickupSuburb = URLDecoder.decode(request.getParameter("pickupsuburb"), "UTF-8");
	    	String pickupState = URLDecoder.decode(request.getParameter("pickupstate"), "UTF-8");
	    	String pickupCountry = URLDecoder.decode(request.getParameter("pickupcountry"), "UTF-8");
	    	String pickupPostcode = request.getParameter("pickuppostcode");
	    	String pickupContactNumber = request.getParameter("pickupcontactnumber");
	    	
	    	System.out.println("**********************pickup name = " + pickupName);
	    	
	    	if(checkIfExistingConsignment(connoteNumber)) {
	    		updateExistingConnote(connoteNumber, parentCarrier, parentDepot, userId);
	    	} else {
	    		createNewConnote(connoteNumber, parentCarrier, parentDepot, userId, 
	    				pickupName, pickupCompany, pickupAddress, pickupArea, pickupSuburb, 
	    				pickupState, pickupCountry, pickupPostcode, pickupContactNumber);
	    	}
	    	finishPickupJob(pickupJobPkId, userPkId, connoteNumber);
	    	break;
	    }
	    
	    try {
//	        out.println(json);
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
	
	private void holdPickupJob(long pickupJobId, long userId) {
		ScsePickupJob job = ScseInitServlet.getPickupJobDao().selectByPrimaryKey(pickupJobId);
		job.setJobStatus("ASSIGNED");
		job.setExifParentUser(userId);
		ScseInitServlet.getPickupJobDao().updateByPrimaryKey(job);
		
		ScseUserPickupJobExample exa = new ScseUserPickupJobExample();
		exa.createCriteria().andParentPickupJobEqualTo(pickupJobId).andCurrentJobEqualTo(true);
		List<ScseUserPickupJob> jobs = ScseInitServlet.getUserPickupJobDAO().selectByExample(exa);
		ScseUserPickupJob userJob = new ScseUserPickupJob();
		for(ScseUserPickupJob j:jobs) {
			try{
				userJob = j;
				j.setCurrentJob(false);
				ScseInitServlet.getUserPickupJobDAO().updateByPrimaryKey(j);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		userJob.setPkId(0l);
		userJob.setCurrentJob(true);
		userJob.setStatus("ASSIGNED");
		userJob.setParentUser(userId);
		userJob.setParentPickupJob(pickupJobId);
		userJob.setNotes("");
		
		ScseInitServlet.getUserPickupJobDAO().insert(userJob);
	}
	
	private void finishPickupJob(long pickupJobId, long userId, String connoteNumber) {
		ScsePickupJob job = ScseInitServlet.getPickupJobDao().selectByPrimaryKey(pickupJobId);
		job.setJobStatus("COMPLETE");
		job.setExifParentUser(userId);
		ScseInitServlet.getPickupJobDao().updateByPrimaryKey(job);
		
		ScseUserPickupJobExample exa = new ScseUserPickupJobExample();
		exa.createCriteria().andParentPickupJobEqualTo(pickupJobId).andCurrentJobEqualTo(true);
		List<ScseUserPickupJob> jobs = ScseInitServlet.getUserPickupJobDAO().selectByExample(exa);
		ScseUserPickupJob userJob = new ScseUserPickupJob();
		for(ScseUserPickupJob j:jobs) {
			try{
				userJob = j;
				j.setCurrentJob(false);
				ScseInitServlet.getUserPickupJobDAO().updateByPrimaryKey(j);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		userJob.setPkId(0l);
		userJob.setCurrentJob(true);
		userJob.setStatus("DONE");
		userJob.setParentUser(userId);
		userJob.setParentPickupJob(pickupJobId);
		userJob.setNotes("完成收件 单号：" + connoteNumber);
		
		ScseInitServlet.getUserPickupJobDAO().insert(userJob);
	}
	
	private boolean checkIfExistingConsignment(String connoteNumber) {
		ScseConsignmentExample exa = new ScseConsignmentExample();
		exa.createCriteria().andConnoteNumberEqualTo(connoteNumber);
		List<ScseConsignment> consignments = ScseInitServlet.getConsignmentDao().selectByExampleWithBLOBs(exa);
		
		return consignments.size() > 0;
	}
	
	private void createNewConnote(String connoteNumber, String carrier, String parentDepot, String userId, String pickupName,
			String pickupCompany, String pickupAddress, String pickupArea, String pickupSuburb, String pickupState, String pickupCountry,
			String pickupPostcode, String pickupContactNumber) {


		ScseStatus status = getPickupStatus(carrier);
		Long parentCarrier = Long.parseLong(carrier);
		
		ScseConsignment consignment = new ScseConsignment();
		consignment.setConnoteNumber(connoteNumber);
		consignment.setAccountCode(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryAddress(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryContactNumber(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryCountry(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryPostcode(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryState(CONNOTE_PLACE_HOLDER);
		consignment.setDeliverySuburb(CONNOTE_PLACE_HOLDER);
		consignment.setDeliveryTime("0:00");
		consignment.setDispatchDate(new Date());
		consignment.setParentCarrier(parentCarrier);
		consignment.setParentUser(Long.parseLong(userId));
		
		consignment.setPickupName(pickupName);
		consignment.setPickupCompany(pickupCompany);
		consignment.setPickupAddress(pickupAddress);
		consignment.setPickupArea(pickupArea);
		consignment.setPickupContactNumber(pickupContactNumber);
		consignment.setPickupCountry(pickupCountry);
		consignment.setPickupPostcode(pickupPostcode);
		consignment.setPickupState(pickupState);
		consignment.setPickupSuburb(pickupSuburb);
		consignment.setPickupTime("0:00");
		
		consignment.setSenderName(pickupName);
		consignment.setSenderCompany(pickupCompany);
		consignment.setSendingAddress(pickupAddress);
		consignment.setSendingAddressCode(CONNOTE_PLACE_HOLDER);
		consignment.setSendingArea(pickupArea);
		consignment.setSendingContactNumber(pickupContactNumber);
		consignment.setSendingCountry(pickupCountry);
		consignment.setSendingPostcode(pickupPostcode);
		consignment.setSendingState(pickupState);
		consignment.setSendingSuburb(pickupSuburb);
		
		consignment.setReceiverName(CONNOTE_PLACE_HOLDER);
		consignment.setRequiredDate(new Date());
		consignment.setSenderName(CONNOTE_PLACE_HOLDER);
		consignment.setServiceLevel(0l);
		consignment.setServiceType(0l);
		consignment.setSpecialRequirements(CONNOTE_PLACE_HOLDER);
		
		consignment.setCost(0d);
		
		consignment.setExifStartDepot(Long.parseLong(parentDepot));
		consignment.setExifCurrentStatus(status.getPkId());
		
		Long parentConsignment = ScseInitServlet.getConsignmentDao().insert(consignment);
		
		try{
			ScseConsignment con = ScseInitServlet.getConsignmentDao().selectByPrimaryKey(parentConsignment);
			createInitialStatusForConsignment(con, parentCarrier, Long.parseLong(parentDepot), userId);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void updateExistingConnote(String connoteNumber, String parentCarrier, String parentDepot, String parentUser) {
		ScseStatus status = getPickupStatus(parentCarrier);
		ScseConsignmentExample exa = new ScseConsignmentExample();
		exa.createCriteria().andConnoteNumberEqualTo(connoteNumber);
		List<ScseConsignment> consignments = ScseInitServlet.getConsignmentDao().selectByExampleWithBLOBs(exa);
		
		if(consignments.size() > 0) {
			ScseConsignment consignment = consignments.get(0);
			long consignmentId = consignment.getPkId();
			
			ScseConsignmentStatusExample csExa = new ScseConsignmentStatusExample();
			csExa.createCriteria().andParentConsignmentEqualTo(consignmentId).andIsCurrentEqualTo(true);
			List<ScseConsignmentStatus> css = ScseInitServlet.getConsignmentStatusDao().selectByExample(csExa);
			for(ScseConsignmentStatus cs:css) {
				try{
					cs.setIsCurrent(false);
					ScseInitServlet.getConsignmentStatusDao().updateByPrimaryKey(cs);
				} catch(Exception ex) {
					
				}
			}
			
			ScseConsignmentStatus cstatus = new ScseConsignmentStatus();
			cstatus.setDescription("完成取件 " + connoteNumber);
			cstatus.setIsCurrent(true);
			cstatus.setParentConsignment(consignmentId);
			cstatus.setParentDepot(Long.parseLong(parentDepot));
			cstatus.setParentStatus(status.getPkId());
			cstatus.setParentUser(Long.parseLong(parentUser));
			
			try{
				ScseInitServlet.getConsignmentStatusDao().insert(cstatus);
			} catch(Exception ex) {
				
			}
			
			consignment.setExifCurrentDepot(Long.parseLong(parentDepot));
			consignment.setExifCurrentStatus(status.getPkId());
			
			try{
				ScseInitServlet.getConsignmentDao().updateByPrimaryKeyWithBLOBs(consignment);
			} catch(Exception ex){}
		}
	}
	
	private ScseStatus getPickupStatus(String parentCarrier) {
		ScseStatusExample exa = new ScseStatusExample();
		exa.createCriteria().andParentCarrierEqualTo(Long.parseLong(parentCarrier)).andIsPickedEqualTo(true);
		
		List<ScseStatus> statuses = ScseInitServlet.getStatusDao().selectByExample(exa);
		
		if(statuses.size() > 0) {
			return statuses.get(0);
		}
		
		return null;
	}
	
	private void createInitialStatusForConsignment(ScseConsignment connote, Long parentCarrier, Long parentDepot, String parentUser) throws Exception {		
		
		ScseStatus status = getPickupStatus(parentCarrier + "");
		
		
		ScseConsignmentStatus conStatus = new ScseConsignmentStatus();
		conStatus.setDescription("已取运单并预生成 " + connote.getConnoteNumber());
		conStatus.setIsCurrent(true);
		conStatus.setParentConsignment(connote.getPkId());
		conStatus.setParentStatus(status.getPkId());
		conStatus.setParentUser(Long.parseLong(parentUser));
		conStatus.setParentDepot(parentDepot);
		
		ScseInitServlet.getConsignmentStatusDao().insert(conStatus);
	}
	
	private void releaseJob(long pickupJobId, long userId) {
		ScsePickupJob job = ScseInitServlet.getPickupJobDao().selectByPrimaryKey(pickupJobId);
		job.setJobStatus("CREATED");
		job.setExifParentUser(0l);
		ScseInitServlet.getPickupJobDao().updateByPrimaryKey(job);
		
		ScseUserPickupJobExample exa = new ScseUserPickupJobExample();
		exa.createCriteria().andParentPickupJobEqualTo(pickupJobId).andCurrentJobEqualTo(true);
		List<ScseUserPickupJob> jobs = ScseInitServlet.getUserPickupJobDAO().selectByExample(exa);
		ScseUserPickupJob userJob = new ScseUserPickupJob();
		for(ScseUserPickupJob j:jobs) {
			try{
				userJob = j;
				j.setCurrentJob(false);
				ScseInitServlet.getUserPickupJobDAO().updateByPrimaryKey(j);
			} catch(Exception ex) {}
		}
		
		userJob.setPkId(0l);
		userJob.setLastUpdated(new Date());
		userJob.setCurrentJob(true);
		userJob.setStatus("RELEASED");
		userJob.setParentUser(userId);
		userJob.setParentPickupJob(pickupJobId);
		userJob.setNotes("");
		
		ScseInitServlet.getUserPickupJobDAO().insert(userJob);
	}
	
	public static void main(String args[]) {
		try{

			ScsePickupJob job = ScseInitServlet.getPickupJobDao().selectByPrimaryKey(10l);
			job.setJobStatus("ASSIGNED");
			ScseInitServlet.getPickupJobDao().updateByPrimaryKey(job);
			
			ScseUserPickupJobExample exa = new ScseUserPickupJobExample();
			exa.createCriteria().andParentPickupJobEqualTo(10l).andCurrentJobEqualTo(true);
			List<ScseUserPickupJob> jobs = ScseInitServlet.getUserPickupJobDAO().selectByExample(exa);
			ScseUserPickupJob userJob = new ScseUserPickupJob();
			for(ScseUserPickupJob j:jobs) {
				try{
					userJob = j;
					j.setCurrentJob(false);
					ScseInitServlet.getUserPickupJobDAO().updateByPrimaryKey(j);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			
			userJob.setPkId(0l);
			userJob.setCurrentJob(true);
			userJob.setStatus("ASSIGNED");
			userJob.setParentUser(5l);
			userJob.setParentPickupJob(10l);
			
			ScseInitServlet.getUserPickupJobDAO().insert(userJob);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
