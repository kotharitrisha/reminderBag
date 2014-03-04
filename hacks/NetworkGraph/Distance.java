/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;
import java.util.*;

import com.wibinet.networks.*;


public class Distance extends Object
{
    public static void main(String[] args) throws IOException
    {
    //System.out.println("I am here"+ args[0]);
    String filename = args[0];
    String disFile ="/Users/linwu/Documents/C/cisco/physicalDistPaj.net";
    String[] items=filename.split("/");
    String fname = items[items.length-1];
    //System.out.println(fname);

	
  //importing physical distance network file
	PajekFileImporter disImporter = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE); 
	System.setIn(new FileInputStream(disFile));
	VisualNetworkData disData = disImporter.readData(System.in);

	//importing network
	PajekFileImporter importer = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE);
	System.setIn(new FileInputStream(args[0]));
	VisualNetworkData nData = importer.readData(System.in);
	
	/*
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

*/		
	DistanceFactory dis = new DistanceFactory();	
	com.wibinet.networks.DistanceFactory.Evaluator disEval = dis.aNewInstance();
	disEval.setNetwork(nData);
	disEval.setDistanceMatrix(disData);
	disEval.runEvaluator();

	/*
	 * System.out.printf("%-20s\t %-10s\t %-10s\t %-10s\t %-10s\t %-20s\t %-20s\t %-20s\t %-20s\t %-20s\t %-10s\t %-10s", 
				"Name",  "ir_In Degree" , "ir_Out Degree", "ir_Mutual", "ir_Size", "BetweennessCentrality", "Constraint", "ConstraintSimple", 
			"IndirectConstraint", "IndirectConstraintSimple", "2StepReach", "totDistance \t maxDistance"); 
			 System.out.println();
	System.out.println();
	*/
	int actorCt = nData.getSize();
		for(int i=0; i<actorCt; i++) {
	    String name = nData.getActor(i).getName();
	    Object val = disEval.evaluateNode(i);
	    System.out.println(fname+"\t"+name+"\t"+val);
	   /* System.out.printf("%-10s\t %-10d\t %-10d\t %-10d\t %-10d\t %-20.4f\t %-20.4f\t%-20.4f\t%-20.4f\t%-20.4f\t %-10.4f\t %-10s",
	    		name, nData.getInDegree(0, i), nData.getOutDegree(0, i), nData.getMutualDegree(i), nData.getEgoSize(i), 
	    		((Double)bcEval.evaluateNode(i)).doubleValue(), ((Double)csfEval.evaluateNode(i)).doubleValue(),
	    		((Double)cfEval.evaluateNode(i)).doubleValue(),   ((Double)icfEval.evaluateNode(i)).doubleValue(), 
	    		((Double)icsfEval.evaluateNode(i)).doubleValue(), 
	    		((Double)sr2Eval.evaluateNode(i)).doubleValue(), val);
		System.out.println();
*/
		}
    }
}
