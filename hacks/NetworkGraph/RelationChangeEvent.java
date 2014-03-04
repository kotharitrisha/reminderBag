/**
 * RelationChangeEvent.java
 *
 * A relation change event should be fired every time something
 * about the set of relations in a network changes.  Examples
 * include (but are not limited to) the addition or deletion of
 * a relation, or a change in a relation name.
 *
 * Copyright (c) 2003 Wibi Internet.
 */

package com.wibinet.networks;

public class RelationChangeEvent extends Object
{
	public final static int SELECTED = 0;
	public final static int INSERTED = 1;
	public final static int DELETED = 2;
	public final static int CHANGED = 3;
	
	protected Object source;
	protected int relIdx;
	protected int type;
	
	public RelationChangeEvent(Object source, int type, int relIdx)
	{
		this.source = source;
		this.type = type;
		this.relIdx = relIdx;
	}
	
	public Object getSource()
	{
		return source;
	}
	
	public int getType()
	{
		return type;
	}

	public int getRelationIndex()
	{
		return relIdx;
	}	
}