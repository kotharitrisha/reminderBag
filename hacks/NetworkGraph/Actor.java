/**
 * Actor.java
 *
 * Hmm...not quite wholly sure this class makes sense, but...
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import com.wibinet.math.*;

public class Actor extends Object
{
	protected NetworkData nData;
	protected String name;
	
	public Actor(NetworkData nData, String name)
	{
		this.nData = nData;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return name;
	}
}
