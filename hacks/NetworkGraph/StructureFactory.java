/**
 * StructureFactory.java
 *
 * A factory that produces NodeEvaluators that perform
 * structural hole analysis a la Burt.
 *
 * Copyright (c) 2002, 2004 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;

public class StructureFactory extends StructureHelper implements NodeEvaluatorFactory
{
	public NodeEvaluator newInstance()
	{
		return new NEvaluator();
	}
  
	public void edit(NodeEvaluator evaluator, JFrame parent)
	{
		if(!(evaluator instanceof Evaluator))
		{
			return; // error?
		}
		super.edit((Evaluator)evaluator, parent);
	}
	
	public boolean isMultiple()
	{
		return false;
	}
	
	public class NEvaluator extends Evaluator implements NodeEvaluator
	{
		public void setNetwork(NetworkData nd)
		{
			this.r = nd.getRelation(0);
		}	  
	    
		public Object evaluateNode(int idx, int ri)
		{
			return evaluateNode(idx);
		}

		public Object evaluateNode(int idx)
		{
			return nodeValues[idx];
		}
		
		public NodeEvaluatorFactory getFactory()
		{
		    return StructureFactory.this;
		}
	}
}
