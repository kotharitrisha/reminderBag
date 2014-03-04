/**
 * AbstractBlockmodelFactory.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.util.Hashtable;

import com.wibinet.gui.*;
import com.wibinet.math.*;

public abstract class AbstractBlockmodelFactory implements BlockmodelFactory
{
	protected int minPrecision;
	protected int maxPrecision;
	
	public AbstractBlockmodelFactory()
	{
		this.minPrecision = 1;
		this.maxPrecision = ExtendedMath.MAX_PRECISION;
	}
	
	public int getMaxPrecision()
	{
		return maxPrecision;
	}
	
	public void setMaxPrecision(int precision)
	{
		this.maxPrecision = precision;
	}
	
	public int getMinPrecision()
	{
		return minPrecision;
	}
	
	public void setMinPrecision(int precision)
	{
		this.minPrecision = precision;
	}

	protected double getStringOrDouble(Hashtable props, String key)
	{
		Object o = props.get(key);
		if(o instanceof String)
		{
			return Double.parseDouble((String)o);
		}
		else
		{
			return ((Double)o).doubleValue();
		}
	}
	
	protected int getStringOrInt(Hashtable props, String key)
	{
		Object o = props.get(key);
		if(o instanceof String)
		{
			return Integer.parseInt((String)o);
		}
		else
		{
			return ((Integer)o).intValue();
		}
	}
}