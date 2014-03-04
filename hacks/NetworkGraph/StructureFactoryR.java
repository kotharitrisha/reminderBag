package com.wibinet.networks;

import javax.swing.*;

public class StructureFactoryR extends StructureHelper implements RelationEvaluatorFactory
{
	public RelationEvaluator newInstance()
	{
		return new REvaluator();
	}
	
	public void edit(RelationEvaluator evaluator, JFrame parent)
	{
		if(!(evaluator instanceof Evaluator))
		{
			return; // error?
		}
		super.edit((Evaluator)evaluator, parent);
	}

	public class REvaluator extends Evaluator implements RelationEvaluator
	{
		public void setRelation(Relation r)
		{
			this.r = r;
		}
	  
		public Object evaluateRelation(int fromIdx, int toIdx)
		{
			return allValues[fromIdx][toIdx];
		}
		
		public RelationEvaluatorFactory getFactory()
		{
		    return StructureFactoryR.this;
		}
	}
}
