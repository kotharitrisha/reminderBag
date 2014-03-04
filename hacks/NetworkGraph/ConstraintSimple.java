/**
 * Constraint.java
 */
package com.wibinet.networks;
import java.io.*;

import com.wibinet.networks.*;


public class ConstraintSimple extends Object
{
    public static void main(String[] args) throws IOException
    {
    System.out.println("I am here"+ args[0]);
	PajekFileImporter importer = 
	    new PajekFileImporter(PajekFileImporter.LIST_TYPE);
	 
	 System.setIn(new FileInputStream(args[0]));
	 
	VisualNetworkData nData = importer.readData(System.in);
	 System.out.println("I am here 2 - after system.in print");
	ConstraintSimpleFactory sr = 
	    new ConstraintSimpleFactory();
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
