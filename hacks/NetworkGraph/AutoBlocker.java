/**
 * AutoBlocker.java
 *
 * Classifies nodes in a graph by automorphic equivalence by specifying
 * the problem as a constraint satisfaction problem, and solving the
 * CSP using a backtracking algorithm.
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;

import com.wibinet.ai.csp.*;
import com.wibinet.ai.datatypes.*;
import com.wibinet.math.Matrix;

public class AutoBlocker implements CSP, NodeDistanceMeasure
{
  protected NetworkData a;
  protected VariableAssignment permutations;
  protected BlockChecker checker;
  protected Graph cg;

  protected boolean useFC     = false;
  protected boolean useNIC    = false;
  protected boolean graphics  = false;
  protected boolean useDVO    = false;
  protected int backtrackerStyle = BT_Backtracker;
  
  public final static int BT_Backtracker = 1;
  public final static int BT_Dynamic = 2;
  public final static int BT_Backjumper = 3;
  
	protected long solveTime;  
  
  public AutoBlocker()
  {
    solveTime = -1;
  }
  
  public void setUseFC(boolean useFC)
  {
    this.useFC = useFC;
  }
  
  public void setUseNIC(boolean useNIC)
  {
    this.useNIC = useNIC;
  }
  
  public void setUseDVO(boolean useDVO)
  {
    this.useDVO = useDVO;
  }
  
  public void setBacktrackerStyle(int backtrackerStyle)
  {
    this.backtrackerStyle = backtrackerStyle;
  }
  
	public double getDistance(NetworkData a, int i, int j)
	{
	  long start_tm = System.currentTimeMillis();
	  this.a = a;
	  this.checker = new BlockChecker();

    // setup variable assignments
    int permSize = a.getSize();
    permutations = new VariableAssignment(permSize);
	  
    // setup domains
    int[] dom = new int[permSize];
    for(int pi=0; pi<permSize; pi++)
    {
      dom[pi] = pi;
    }
    IntSet domain = new IntSet(dom);
    domain.remove(i);
    domain.remove(j);
    for(int pi=0; pi<permSize; pi++)
    {
      permutations.setDomain(pi, domain);
    }
    
    // fix assignments on a swap
    permutations.setValue(j, i);
    permutations.setValue(i, j);
    permutations.fixVariable(j);
    permutations.fixVariable(i);
    
    // make a constraint graph, connect all points *that are connected in underlying graph*
    cg = new Graph(permSize);
    for(int n1=0; n1<permSize; n1++)
    {
	    for(int n2=0; n2<permSize; n2++)
      {
        if(((a.getTieStrength(n1, n2) != 0.0) ||
            (a.getTieStrength(n2, n1) != 0.0)) && 
           (n1 != n2))
        {
          cg.addEdge(new Edge(n1, n2));
          cg.addEdge(new Edge(n2, n1));
        }
      }
    }

    if(useNIC)
    {
      // enforce inverse consistency
      NICEnforcer nic = new NICEnforcer(this);
      boolean possible = nic.enforce();
      if(!possible)
      {
		    solveTime = System.currentTimeMillis() - start_tm;
        return 1.0; // maxium distance?
      }
    }
    
    Backtracker bt = null;
    switch(backtrackerStyle)
    {
      case BT_Dynamic:
      	bt = new DynamicBacktracker(this, graphics, a.getSize());;
      	break;
      	
     	case BT_Backjumper:
     		bt = new CDBackJumper(this, graphics);
     		break;
     	
     	default:
     		bt = new Backtracker(this, graphics);
     		break;
    }
    
    if(useFC)
    {
      // use forward checking
      ForwardChecker fc = new ForwardChecker(this);
      bt.setForwardChecker(fc);
    }

    bt.setUseDVO(useDVO); // set dynamic variable ordering
    bt.solve();
    solveTime = System.currentTimeMillis() - start_tm;
    if(bt.getStatus() == Backtracker.STATUS_IMPOSSIBLE)
    {
      return 1.0; // max distance?
      // System.out.println("Positions "+r+" and " +c+" not automorphically equivalent");
    }
    else
    {
      return 0.0; // min distance?
      // System.out.println("Positions "+r+" and " +c+" automorphically equivalent");
    }
  }
	
  public VariableAssignment getAssignment()
  {
    return permutations;
  }
  
  public ConstraintChecker getConstraintChecker()
  {
    return checker;
  }
  
  public Graph getConstraintGraph()
  {
    return cg;
  }
  
  public void display()
  {
    String out = "[ " + permutations.getValue(0);
    for(int i=1; i<a.getSize(); i++)
    {
      if(permutations.isAssigned(i))
      {
        out += " " + permutations.getValue(i);
      }
      else
      {
        out += " x";
      }
    }
    System.out.println("perms: " + out + "]");
  }

  public int getCheckCalls()
  {
    return checker.getCheckCalls();
  }
  
  public int getOperationCount()
  {
    return checker.getCheckCalls();
  }
  
  public long getSolveTime()
  {
    return solveTime;
  }

  public String varName(int var)
  {
    return "var [" + var + "]";
  }
  
  public String valueLabel(int value)
  {
    return "" + value;
  }
  
  public class BlockChecker implements ConstraintChecker
  {
    protected int checkCt = 0;
	  
	  public boolean pairwiseCheck(VariableAssignment x, int var1, int var2)
	  {
	    checkCt++;
	    
	    // let's review what a variable assignment means...
	    // assigning variable [i] to value [x] means move the [x]th row and column
	    // to the [i]th row and column...
	    int from1 = x.getValue(var1);
	    int from2 = x.getValue(var2);
	    return ((a.getTieStrength(var1, var2) == a.getTieStrength(from1, from2)) &&
	            (a.getTieStrength(var2, var1) == a.getTieStrength(from2, from1)) &&
	            (from1 != from2));
	  }
	  
	  public int getCheckCalls()
	  {
	    return checkCt;
	  }
  }
}