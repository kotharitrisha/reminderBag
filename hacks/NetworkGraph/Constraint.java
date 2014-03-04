/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;
import java.sql.Timestamp;

import com.wibinet.networks.*;


public class Constraint extends Object
{
    public static void main(String[] args) throws IOException
    {
    	long start = System.currentTimeMillis();
	PajekFileImporter importer = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE);
	 
	 System.setIn(new FileInputStream(args[0]));
	 
	VisualNetworkData nData = importer.readData(System.in);
	 
	ConstraintFactory sr = 
	    new ConstraintFactory();
	NodeEvaluator srEval = sr.newInstance();
	srEval.setNetwork(nData);
	srEval.runEvaluator();

	int actorCt = nData.getSize();
	 //System.out.println("I am here 2 - after ndata.getsize");
	for(int i=0; i<actorCt; i++) {
	    String name = nData.getActor(i).getName();
	    Object val = srEval.evaluateNode(i);
	    if(true) System.out.println(name+"\t"+val);
	}
	
	System.out.println(System.currentTimeMillis()- start);
    }
}
