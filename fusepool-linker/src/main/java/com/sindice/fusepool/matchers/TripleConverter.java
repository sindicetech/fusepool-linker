/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.matchers;

/**
 * Triple converter for use in conjunction with the {@link CollectingMatchListener}
 * 
 *
 * @param <T> The the type of the result of the conversion.
 */
public interface TripleConverter<T> {
	public T convert(SimpleTriple triple);
}
