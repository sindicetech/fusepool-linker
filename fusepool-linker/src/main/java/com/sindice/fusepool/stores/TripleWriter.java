/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.stores;

import java.io.Closeable;

import com.sindice.fusepool.matchers.SimpleTriple;

public interface TripleWriter extends Runnable, Closeable {
	public boolean add(SimpleTriple triple);
	public void init();
}
