/**
 * NodeEvaluatorFactory.java
 *
 * An evaluator factory serves to generate new instances of NodeEvaluator
 * objects, to define their general properties, and to edit their specific
 * properties.
 *
 * (c) 2000 Wibi Internet
 */
 
package com.wibinet.networks;

import javax.swing.*;

public interface NodeEvaluatorFactory
{
  public NodeEvaluator newInstance();
  public void edit(NodeEvaluator evaluator, JFrame parent);
  public Class getEvaluatorClass();
  public String getName();
  public String getGroup();
	public boolean isMultiple();
}