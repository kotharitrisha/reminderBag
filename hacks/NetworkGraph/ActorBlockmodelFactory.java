/**
 * ActorBlockmodelFactory.java
 *
 * Need this type to distinguish between those factories that dispense
 * actor blockmodels and those that do not...
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.math.Partition;

public interface ActorBlockmodelFactory extends BlockmodelFactory
{
	public ActorBlockmodel newActorBlockmodelInstance(NetworkData nData, Partition p);
}