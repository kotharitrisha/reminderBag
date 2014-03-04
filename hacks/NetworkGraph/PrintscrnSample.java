/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;
import java.util.*;

import com.wibinet.networks.*;


public class PrintscrnSample extends Object
{
    public static void main(String[] args) throws IOException
    {
    //System.out.println("I am here"+ args[0]);
    String filename = args[0];
     String[] items=filename.split("/");
    String fname = items[items.length-1];
    //System.out.println(fname);
    fname =fname.replaceAll("_.*", "");
    fname =fname.replaceAll("\\..*", "");
    fname =fname.replaceAll("M", "");
    fname =fname.replaceAll("Q", "");
    fname =fname.replaceAll("I", "");
   //import financial data 
   
	HashMap<String, Double> monthlyRev = new  HashMap<String, Double>();
	HashMap<String, Double> monthlyHour = new HashMap<String, Double> ();
	HashMap<String, String> divisions = new HashMap<String, String>();
	HashMap<String, Integer> mgrs = new HashMap<String, Integer>();
	HashMap<String, Integer> adoptions = new HashMap<String, Integer>();
	HashMap<String, Integer> srs9 = new HashMap<String, Integer>();
	HashMap<String, Integer> srs10 = new HashMap<String, Integer>();
	
	
	
    
       
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
				"IndirectConstraint", "minFrdIdc", "IndirectConstraintSimple", "minFrdIdcs", "2StepReach", "3StepReach"); 
	//System.out.printf("%-10s\t %-20s\t %-10s \t %-10s", "Date",	"Email", "numfAdopt", "fAdoptDate");			 
	System.out.println();
				 
	for(int i=0; i<actorCt; i++) {
	    String name = nData.getActor(i).getName();
	    ArrayList<Double> val = (ArrayList<Double>)fcfEval.evaluateNode(i);
	    
	
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
	    		((Double)sr2Eval.evaluateNode(i)).doubleValue(), ((Double)sr3Eval.evaluateNode(i)).doubleValue());
	        
	
	System.out.println();
	 }
    }
}
