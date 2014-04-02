/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.stores;

public class TripleStorageException extends RuntimeException {
	public TripleStorageException(String string) {
		super(string);
	}

	public TripleStorageException(String string, Throwable t) {
		super(string, t);
	}

	private static final long serialVersionUID = 1L;
}
