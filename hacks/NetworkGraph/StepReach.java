/**
 * 2StepReach.java
 */
package com.wibinet.networks;
import java.io.*;

import com.wibinet.networks.*;

/* to run:
 * args[0] file name that you would like to use as input
 * args[1] # of step reach
 * args[2] Directionality: 1 is bidirectional, 0 is directed only toward out neighborhood
 */

public class StepReach extends Object
{
    public static void main(String[] args) throws IOException
    {
    System.out.println("I am here"+ args[0]);
	PajekFileImporter importer = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE);
	 
	 System.setIn(new FileInputStream(args[0]));
	 
	VisualNetworkData nData = importer.readData(System.in);
	 System.out.println("I am here 2 - after system.in print");
	StepReachFactory sr = 
		new StepReachFactory("2", "1");
	    //new StepReachFactory(args[1], args[2]);
	NodeEvaluator srEval = sr.newInstance();
	srEval.setNetwork(nData);
	srEval.runEvaluator();

	int actorCt = nData.getSize();
	 System.out.println("I am here 2 - after ndata.getsize");
	for(int i=0; i<actorCt; i++) {
	    String name = nData.getActor(i).getName();
	    Object val = srEval.evaluateNode(i);
	    System.out.println(name+"\t"+val);
	}
    }
}
