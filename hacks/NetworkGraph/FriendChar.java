/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;
import java.util.*;

import com.wibinet.networks.*;


public class FriendChar extends Object
{
    public static void main(String[] args) throws IOException
    {
    System.out.println("I am here"+ args[0]);
    String filename = args[0];
    String finFile ="/Users/linwu/Documents/T/sblue/egoJan2009/person_month_bpg_sub.csv";
    String mgrFile ="/Users/linwu/Documents/T/sblue/egoJan2009/isMgrSub.csv";
    String divFile = "/Users/linwu/Documents/T/sblue/egoJan2009/divisionsSub.csv";
     String[] items=filename.split("/");
    String fname = items[items.length-1];
    //System.out.println(fname);
    fname =fname.replaceAll("_.*", "");
    fname =fname.replaceAll("\\..*", "");
    fname =fname.replaceAll("M", "");
    System.out.println(fname);
    
   //import financial data 
    FileInputStream finance = new FileInputStream(finFile);
    InputStreamReader financer = new InputStreamReader(finance);
	BufferedReader inFin = new BufferedReader(financer, 8096);
	//import manager data
	FileInputStream mgrfs = new FileInputStream(mgrFile);
	InputStreamReader mgrfsr = new InputStreamReader(mgrfs);
	BufferedReader inMgr = new BufferedReader(mgrfsr, 8096);
	//import division data
	FileInputStream divfs = new FileInputStream(divFile);
	InputStreamReader divfsr = new InputStreamReader(divfs);
	BufferedReader inDiv = new BufferedReader(divfsr, 8096);

	HashMap<String, Double> monthlyRev = new  HashMap<String, Double>();
	HashMap<String, Double> monthlyHour = new HashMap<String, Double> ();
	HashMap<String, String> divisions = new HashMap<String, String>();
	HashMap<String, Integer> mgrs = new HashMap<String, Integer>();
	
	// read lines, break on sections
	String line = inMgr.readLine().trim();
	int lineno = 1;
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inMgr.readLine();
			lineno++;

		}
		else {
			
			StringTokenizer st = new StringTokenizer(line,",");
			String email = st.nextToken();
			String strIsMgr = st.nextToken();
			email = email.replaceAll("\"", "");
			strIsMgr= strIsMgr.replaceAll("\"", "");
			int isMgr = 0;
			if(strIsMgr.equalsIgnoreCase("y")){ isMgr = 1;}
			if(!mgrs.containsKey(email)){
				mgrs.put(email, new Integer(isMgr));
			}				
			line = inMgr.readLine();
		}
	}
	// reading division file
	System.out.println("division file");
	line = inDiv.readLine().trim();
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inMgr.readLine();
			lineno++;

		}
		else {
			StringTokenizer st = new StringTokenizer(line, ",");
			String email = st.nextToken();
			email = email.replaceAll("\"", "");
			String primDiv = st.nextToken();	
			primDiv = primDiv.replaceAll("\"", "");
			if(!divisions.containsKey(email)){
				divisions.put(email, primDiv);
			}		
			line = inDiv.readLine();
		}
	}
	// reading financial file
	System.out.println("financial file");
	line = inFin.readLine();
	line = inFin.readLine().trim();
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inMgr.readLine();
			lineno++;

		}
		else {
			StringTokenizer st = new StringTokenizer(line, ",");
			String sernum = st.nextToken();
			Integer date =new Integer(st.nextToken());
	
			if(date.compareTo(new Integer(fname))==0){
				//System.out.println(line);
				//System.out.println(date+ "  "+ new Integer(fname));
				String personDate = st.nextToken();
				Double rev_pm = new Double(st.nextToken());
				Double hours_pm = new Double(st.nextToken());
				String email = st.nextToken();
				email = email.replaceAll("\"", "");
				
				if(!monthlyRev.containsKey(email)){
					monthlyRev.put(email, rev_pm);
				}
			
				if(!monthlyHour.containsKey(email)){
					monthlyHour.put(email, hours_pm);
				}
			
			
				//System.out.println(email+ " "+ rev_pm+ " " + hours_pm);
			}
		line = inFin.readLine();
		}
	} 
	PajekFileImporter importer = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE);
	 
	 System.setIn(new FileInputStream(args[0]));
	 
	VisualNetworkData nData = importer.readData(System.in);
	
	FriendCharFactory fcf = new FriendCharFactory();
	 
	com.wibinet.networks.FriendCharFactory.Evaluator fcfEval = fcf.aNewInstance();
	fcfEval.setNetwork(nData);
	fcfEval.setRevenues(monthlyRev);
	fcfEval.setHours(monthlyHour);
	fcfEval.setDivisions(divisions);
	fcfEval.setMgr(mgrs);
	fcfEval.runEvaluator();

	int actorCt = nData.getSize();
	// System.out.println("I am here 2 - after ndata.getsize");

				 
	for(int i=0; i<actorCt; i++) {
	  
	   ArrayList<Double> val = (ArrayList<Double>)fcfEval.evaluateNode(i);
	   String out="";
	   for (int j = 0; j<val.size(); j++){
		   out +=" "+ val.get(j).doubleValue();
	   }
	  System.out.println(i +" " +nData.getActor(i).getName()+" " + out);
	   }
	System.out.println();
	 }
    }

