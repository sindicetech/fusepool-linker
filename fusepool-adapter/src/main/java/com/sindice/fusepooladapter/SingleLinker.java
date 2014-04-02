/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepooladapter;

import org.apache.clerezza.rdf.core.TripleCollection;

/**
 * Temporarily interface intended to link records in one dataset 
 * 
 *
 */
public interface SingleLinker {
	public TripleCollection interlink(TripleCollection dataToInterlink);

}
