/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.stores;

import java.io.IOException;
import java.util.HashSet;

import com.sindice.fusepool.matchers.SimpleTriple;

public class SetTripleWriter extends HashSet<SimpleTriple> implements TripleWriter {
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean add(SimpleTriple triple) {
		return super.add(triple);
	}

	@Override
	public void init() {
	}

}
