/**
 * RelationEvaluatorFactory.java
 *
 * An evaluator factory serves to generate new instances of RelationEvaluator
 * objects, to define their general properties, and to edit their specific
 * properties.
 *
 * (c) 2000, 2001 Wibi Internet
 */
 
package com.wibinet.networks;

import javax.swing.*;
import com.wibinet.app.*;

public interface RelationEvaluatorFactory
{
  // tricky...
  public final static RelationEvaluatorFactory[] Factories = (RelationEvaluatorFactory[])
      Application.getPlugIns("com.wibinet.networks.RelationEvaluatorFactory");

  public RelationEvaluator newInstance();
  public void edit(RelationEvaluator evaluator, JFrame parent);
  public Class getEvaluatorClass();
  public String getName();
  public String getGroup();
}