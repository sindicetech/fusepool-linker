/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.matchers;

/**
 * Returns the triple that it receives without any conversion. 
 */
public class IdentityTripleConverter implements TripleConverter<SimpleTriple> {
	public SimpleTriple convert(SimpleTriple triple) {
		return triple;
	}
}
