/** * MDLScore.java * * A class for computing MDL scores for network data. * * (c) 2000 Wibi Internet */package com.wibinet.networks;import com.wibinet.math.ExtendedMath;import com.wibinet.math.Matrix;public class MDLScore extends Object // change this to NetworkMeasure or sum'n{  public static double getDescriptionLength(Blockmodel bModel)  {		// get model bits...    double modelLength = -bModel.getLgModelProbability();        // get data bits...    double dataBits = -bModel.getLgDataProbability();    return modelLength + dataBits;  }    /*private void deejna()  {    // calculate 'identity adjustment'    double identAdjust = 0.0;    for(int ri=0; ri<r; ri++)    {      for(int relBlock=0; relBlock<B; relBlock++)      {      	int trueTies = 0;      	int[] relActors = p.getActors(pIndices[relBlock]);      	for(int rai=0; rai<relActors.length; rai++)      	{      		trueTies += ((nData.getTieStrength(ri, ai, relActors[rai]) == 1.0) ? 1 : 0);      	}       	long possibleIds = ExtendedMath.choose(bsize[relBlock], trueTies);       	identAdjust += ExtendedMath.lg((double)possibleIds);      }    }  } */}