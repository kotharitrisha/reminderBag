/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;
import java.util.*;

import com.wibinet.networks.*;


public class Printscrn extends Object
{
    public static void main(String[] args) throws IOException
    {
    //System.out.println("I am here"+ args[0]);
    String filename = args[0];
    String finFile ="/Users/lynnwu/Documents/T/sblue/egoJan2009/person_month_bpg_sub.csv";
    String mgrFile ="/Users/lynnwu/Documents/T/sblue/egoJan2009/isMgrSub.csv";
    String srFile ="/Users/lynnwu/Documents/T/sblue/egoJan2009/isSenior.csv";
    String divFile = "/Users/lynnwu/Documents/T/sblue/egoJan2009/divisionsSub.csv";
    String adoptionFile ="/Users/lynnwu/Documents/T/sblue/egoJan2009/signupDate.csv";
     String[] items=filename.split("/");
    String fname = items[items.length-1];
    //System.out.println(fname);
    fname =fname.replaceAll("_.*", "");
    fname =fname.replaceAll("\\..*", "");
    fname =fname.replaceAll("M", "");
    fname =fname.replaceAll("Q", "");
    fname =fname.replaceAll("I", "");
   //import financial data 
    FileInputStream finance = new FileInputStream(finFile);
    InputStreamReader financer = new InputStreamReader(finance);
	BufferedReader inFin = new BufferedReader(financer, 8096);
	//import manager data
	FileInputStream mgrfs = new FileInputStream(mgrFile);
	InputStreamReader mgrfsr = new InputStreamReader(mgrfs);
	BufferedReader inMgr = new BufferedReader(mgrfsr, 8096);
	//import senior rank data
	FileInputStream srfs = new FileInputStream(srFile);
	InputStreamReader srfsr = new InputStreamReader(srfs);
	BufferedReader insr = new BufferedReader(srfsr, 8096);
	//import division data
	FileInputStream divfs = new FileInputStream(divFile);
	InputStreamReader divfsr = new InputStreamReader(divfs);
	BufferedReader inDiv = new BufferedReader(divfsr, 8096);
	//import signupDates
	FileInputStream adopt = new FileInputStream(adoptionFile);
	InputStreamReader adoptr = new InputStreamReader(adopt);
	BufferedReader inAdopt= new BufferedReader(adoptr, 8096);
	
	HashMap<String, Double> monthlyRev = new  HashMap<String, Double>();
	HashMap<String, Double> monthlyHour = new HashMap<String, Double> ();
	HashMap<String, String> divisions = new HashMap<String, String>();
	HashMap<String, Integer> mgrs = new HashMap<String, Integer>();
	HashMap<String, Integer> adoptions = new HashMap<String, Integer>();
	HashMap<String, Integer> srs9 = new HashMap<String, Integer>();
	HashMap<String, Integer> srs10 = new HashMap<String, Integer>();
	
	
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
	//System.out.println("division file");
	line = inDiv.readLine().trim();
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inDiv.readLine();
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
	// reading sign-up/adoption file
	//System.out.println("adoption file");
	line = inAdopt.readLine().trim();
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inAdopt.readLine();
			lineno++;

		}
		else {
			StringTokenizer st = new StringTokenizer(line, ",");
			String email = st.nextToken();
			email = email.replaceAll("\"", "");
			Integer signupMonth = new Integer(st.nextToken());	
			if(!adoptions.containsKey(email)){
				adoptions.put(email,signupMonth);
			}		
			line = inAdopt.readLine();
		}
	}
		
	// reading senior file: isSenior10: mgr + band10 or above; isSenior9: mgr + band9 or above
	//System.out.println("adoption file");
	line = insr.readLine().trim(); // the first line is the headers
	line = insr.readLine().trim();
		
	while(line != null) 	{	
		// blank	 line
		if(line.length()==0) {
			
			// skip
			line = insr.readLine();
			lineno++;
			
		}
		else {
			StringTokenizer st = new StringTokenizer(line, ",");
			String email = st.nextToken();
			email = email.replaceAll("\"", "");
			Integer sr10 = new Integer(st.nextToken());	
			Integer sr9 = new Integer(st.nextToken());	
			if(!srs10.containsKey(email)){
				srs10.put(email,sr10);
			}		
			if(!srs9.containsKey(email)){
				srs9.put(email,sr9);
			}
			line = insr.readLine();
		}	
	}	
		
	// reading financial file
	//System.out.println("financial file");
	line = inFin.readLine();
	line = inFin.readLine().trim();
	while(line != null) {
		// blank line
		if(line.length()==0) {

			// skip
			line = inFin.readLine();
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
				if(email.equals("tjkeyser@us,ibm.com")){
					System.out.println(email+ " "+ rev_pm);
				}
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
	BetweennessCentralityFactory bc = new BetweennessCentralityFactory();
	 ConstraintFactory cf = 	 new ConstraintFactory();
	ConstraintSimpleFactory csf =  new ConstraintSimpleFactory();
	IndirectConstraintFactory icf = new IndirectConstraintFactory();
	IndirectConstraintSimpleFactory icsf = new IndirectConstraintSimpleFactory();

	StepReachFactory sr2 = new StepReachFactory("2", "1");
	StepReachFactory sr3 = new StepReachFactory("3", "1");

	NodeEvaluator bcEval = bc.newInstance();
	bcEval.setNetwork(nData);
	bcEval.runEvaluator();
	
	NodeEvaluator cfEval = cf.newInstance();
	cfEval.setNetwork(nData);
	cfEval.runEvaluator();
	
	NodeEvaluator csfEval = csf.newInstance();
	csfEval.setNetwork(nData);
	csfEval.runEvaluator();

	NodeEvaluator icfEval = icf.newInstance();
	icfEval.setNetwork(nData);
	icfEval.runEvaluator();
	
	NodeEvaluator icsfEval = icsf.newInstance();
	icsfEval.setNetwork(nData);
	icsfEval.runEvaluator();
	
	NodeEvaluator sr2Eval = sr2.newInstance();
	sr2Eval.setNetwork(nData);
	sr2Eval.runEvaluator();
	
	NodeEvaluator sr3Eval = sr3.newInstance();
	sr3Eval.setNetwork(nData);
	sr3Eval.runEvaluator();

	
	FriendCharFactory fcf = new FriendCharFactory();
	com.wibinet.networks.FriendCharFactory.Evaluator fcfEval = fcf.aNewInstance();
	fcfEval.setNetwork(nData);
	fcfEval.setRevenues(monthlyRev);
	fcfEval.setHours(monthlyHour);
	fcfEval.setDivisions(divisions);
	fcfEval.setMgr(mgrs);
	fcfEval.setAdoption(adoptions);
	fcfEval.setSr9(srs9);
	fcfEval.setSr10(srs10);
	fcfEval.setDates((new Integer(fname)).intValue());
	fcfEval.runEvaluator();

	int actorCt = nData.getSize();
	// System.out.println("I am here 2 - after ndata.getsize");
	 
	
	
	System.out.printf("%-10s\t %-20s\t %-10s\t %-10s\t %-10s\t %-10s\t %-20s\t %-20s\t %-20s\t %-20s\t %-20s\t %-20s\t %-20s\t%-10s\t %-10s\t %-60s", 
				"Date",	"Email",  "In Degree" , "Out Degree", "Mutual", "Size", "BetweennessCentrality", "Constraint", "ConstraintSimple", 
				"IndirectConstraint", "minFrdIdc", "IndirectConstraintSimple", "minFrdIdcs", "2StepReach", "3StepReach", "fmonthlyRev \t fmonthlyHour \t fmgrs \t fdivs \t numAdopt \t adoptDate \t senior9 \t senior10"); 
	//System.out.printf("%-10s\t %-20s\t %-10s \t %-10s", "Date",	"Email", "numfAdopt", "fAdoptDate");			 
	System.out.println();
				 
	for(int i=0; i<actorCt; i++) {
	    String name = nData.getActor(i).getName();
	    ArrayList<Double> val = (ArrayList<Double>)fcfEval.evaluateNode(i);
	    String out="";
	    for (int j = 0; j<val.size(); j++){	
	    	out +=val.get(j).doubleValue()+"\t";
	    }
	int numAdopt = val.get(val.size()-2).intValue();
	int adoptDate = val.get(val.size()-1).intValue();
	  //  System.out.printf( "%-10s\t %-20s\t %-10s\t %-10s",
	   // 		fname, name+".ibm.com ", numAdopt, adoptDate);		
	ArrayList<Double> idc = (ArrayList<Double>)icfEval.evaluateNode(i);
	double idcv = idc.get(0).doubleValue();
	double minFrIdc = idc.get(1).doubleValue();
	ArrayList<Double> idcs = (ArrayList<Double>)icsfEval.evaluateNode(i);
	double idcsv = idcsv = idcs.get(0).doubleValue();
	double minFrIdcsv = idcs.get(1).doubleValue();
	
	    System.out.printf("%-10s\t %-20s\t %-10d\t %-10d\t %-10d\t %-10d\t %-20.4f\t %-20.4f\t %-20.4f\t %-20.4f\t %-20.4f\t %-20.4f\t%-20.4f\t%-20.4f\t%-20.4f\t %-20s",
	    		fname, name+".ibm.com", nData.getInDegree(0, i), nData.getOutDegree(0, i), nData.getMutualDegree(i), nData.getEgoSize(i), 
	    		((Double)bcEval.evaluateNode(i)).doubleValue(), ((Double)csfEval.evaluateNode(i)).doubleValue(),
	    		((Double)cfEval.evaluateNode(i)).doubleValue(),   idcv, minFrIdc, idcsv, minFrIdcsv,
	    		((Double)sr2Eval.evaluateNode(i)).doubleValue(), ((Double)sr3Eval.evaluateNode(i)).doubleValue(), out);
	        
	
	System.out.println();
	 }
    }
}
