/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepooladapter.storage;

import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.jena.tdb.storage.TdbTcProvider;
import org.apache.felix.scr.annotations.Service;

/**
 * class that helps running TCManager out of the clezzera framework 
 */
@Service(WeightedTcProvider.class)
public class TdbWrapper  extends TdbTcProvider implements  WeightedTcProvider{
	 private int weight = 80;
	public TdbWrapper(){
		super.activate(null);
	}
	@Override
    public int getWeight() {
        return weight;
    }
}
