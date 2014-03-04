/**
 * NormExBlockmodelFactory.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;
import com.wibinet.math.Partition;

import java.util.*;

public class NormExBlockmodelFactory extends AbstractBlockmodelFactory
  implements ActorBlockmodelFactory
{
	protected Hashtable props;
	
	public NormExBlockmodelFactory()
	{
		this.props = new Hashtable();
	}
	
	public Blockmodel newInstance(NetworkData nData, com.wibinet.math.Partition p)
	{
		return new NormExBlockmodel(nData, p);
	}
	
	public ActorBlockmodel newActorBlockmodelInstance(NetworkData nData, com.wibinet.math.Partition p)
	{
		return (ActorBlockmodel)newInstance(nData, p);
	}
	
	public boolean edit(Blockmodel model)
	{
		return true;
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}

	public void setProperties(Blockmodel model, Hashtable props)
	{
	}

	public String getType()
	{
		return "Normal Exrege";
	}
}