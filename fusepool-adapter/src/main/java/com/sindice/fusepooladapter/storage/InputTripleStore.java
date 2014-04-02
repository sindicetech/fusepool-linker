/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepooladapter.storage;

import org.apache.clerezza.rdf.core.TripleCollection;
/**
 * interface for input triple store 
 *
 */
public interface InputTripleStore {
  public int populate(TripleCollection triples);
  public void init();

}
