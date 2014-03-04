/**
 * AutomorphicFactory.java
 *
 * This factory generates node evaluators that classify nodes using an
 * automorphic equivalence criteria.
 *
 * (c) 2000 Wibi Internet
 */
 
package com.wibinet.networks;

import javax.swing.*;

public class AutomorphicFactory implements NodeEvaluatorFactory
{
  public NodeEvaluator newInstance()
  {
    return new AutomorphicEquivalenceEvaluator();
  }
  
  public void edit(NodeEvaluator evaluator, JFrame parent)
  {
    if(!(evaluator instanceof AutomorphicEquivalenceEvaluator))
    {
      return; // error?
    }
  }
  
  public Class getEvaluatorClass()
  {
    return String.class;
  }
  
  public String getName()
  {
    return "Automorphic Equivalence";
  }
  
  public String getGroup()
  {
    return "Equivalence";
  }
  
	public boolean isMultiple()
	{
		return false;
	}
	
  public class AutomorphicEquivalenceEvaluator implements NodeEvaluator
  {
    protected NetworkData nd;
    protected String name;
    protected String[] classes;
    protected AutoBlocker blocker;
    
    public AutomorphicEquivalenceEvaluator()
    {
      this.nd = null;
      this.name = "AutoEquiv";
      this.classes = new String[0];
      blocker = new AutoBlocker();
    }
    
    public void runEvaluator()
    {
      NodeClassifier nc = new NodeClassifier(blocker);
		  int[] equivClasses = nc.getEquivalenceClasses(nd);
		  classes = new String[equivClasses.length];
		  for(int ci=0; ci<classes.length; ci++)
		  {
		    classes[ci] = "Class " + equivClasses[ci];
		  }
    }
    
	  public void setNetwork(NetworkData nd)
	  {
	    this.nd = nd;
	  }
	  
	  public Object evaluateNode(int idx, int ri)
	  {
		  return evaluateNode(idx);
	  }
	  
	  public Object evaluateNode(int idx)
	  {
	    if(idx >= classes.length)
	    {
	      return "Undefined";
	    }
	    return classes[idx];
	  }
	  
	  public String getName()
	  {
	    return name;
	  }
	  
	  public NodeEvaluatorFactory getFactory()
	  {
	    return AutomorphicFactory.this;
	  }
  }
}