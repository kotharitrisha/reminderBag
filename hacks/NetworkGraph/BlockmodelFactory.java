/**
 * BlockmodelFactory.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.util.*;
import com.wibinet.math.Partition;

public interface BlockmodelFactory
{
	public Blockmodel newInstance(NetworkData nData, com.wibinet.math.Partition p);
	public boolean edit(Blockmodel model);
	public Hashtable getProperties(Blockmodel model);
	public void setProperties(Blockmodel model, Hashtable props);
	public String getType();
	
	public int getMaxPrecision();
	public void setMaxPrecision(int precision);
	public int getMinPrecision();
	public void setMinPrecision(int precision);
}