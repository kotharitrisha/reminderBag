/**
 * MDLMeasureFactory.java
 *
 * Copyright (c) 2000-2004 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.util.*;
import com.wibinet.math.BasicPartition;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Partition;

public class MDLMeasureFactory implements ModelMeasureFactory
{
	protected BlockmodelFactory modelFactory;
	
  public MDLMeasureFactory(BlockmodelFactory modelFactory)
  {
  	this.modelFactory = modelFactory;
  }
  
  public BlockmodelFactory getBlockmodelFactory()
  {
  	return modelFactory;
  }
  
  public String getName()
  {
    return modelFactory.getType() + "/MDL";
  }
  
  public ModelMeasure newInstance(NetworkData nData)
  {
  	// need a model just to get the properties
    Blockmodel model = modelFactory.newInstance(nData, nData.getPartition());
    modelFactory.edit(model);
    Hashtable props = modelFactory.getProperties(model);
    return new MDLModelMeasure(model, props);
  }
  
  public class MDLModelMeasure implements ModelMeasure
  {
		protected boolean knowWorstScore;
	  protected double worstScore;
	  protected NetworkData nData;
	  protected Hashtable props;

  	public MDLModelMeasure(Blockmodel model, Hashtable properties)
  	{
	    this.nData = model.getNetwork();
	    this.props = properties;
			this.knowWorstScore = false;
			this.worstScore = Double.NaN;
	    
  	}
  	
	  public double getWorstScore()
	  {
			// do we know worst score
			if(!knowWorstScore)
			{
				// create the 'null' partition, every actor in its own block, and
				// the 'one' partition, every actor in the same block.  Hopefully(?)
				// one of these will be the worst...(entropy tends to have a negative
				// second derivative with respect to n?)
				BasicPartition nullP = new BasicPartition(nData);
				BasicPartition oneP = new BasicPartition(nData);
				int numActors = nData.getSize();
				for(int ai=0; ai<numActors; ai++)
				{
					nullP.setPartition(ai, ai);
					oneP.setPartition(ai, 0);
				}
				Blockmodel nullModel = modelFactory.newInstance(nData, nullP);
				modelFactory.setProperties(nullModel, props);
				nullModel.setPrecision(ExtendedMath.MAX_PRECISION);
				Blockmodel oneModel = modelFactory.newInstance(nData, oneP);
				modelFactory.setProperties(oneModel, props);
				oneModel.setPrecision(ExtendedMath.MAX_PRECISION);
				double nullScore = getScore(nullModel);
				double oneScore  = getScore(oneModel);

				worstScore = Math.max(nullScore, oneScore);
				knowWorstScore = true;
			}

	  	return worstScore;
	  }
  
	  public double getBestScore()
	  {
			return 0.0;  	
	  }
	  
	  public double getScore(Blockmodel model)
	  {
	  	modelFactory.setProperties(model, props);
	  	model.compute(null); // don't forget to compute!
	  	return MDLScore.getDescriptionLength(model);
	  }
	}
}