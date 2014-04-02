/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.cleaners;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AddressLocalityCleanerTest {

	@Test
	public void testClean() {
		AddressLocalityCleaner cleaner = new AddressLocalityCleaner();
		assertEquals("6200 Wiesbaden 1",cleaner.clean("D-6200 Wiesbaden 1"));
		assertEquals("Reading, Berkshire RG1 8EQ",cleaner.clean("GB-Reading, Berkshire RG1 8EQ"));
		assertEquals("London EC4Y 1LL'",cleaner.clean("London EC4Y 1LL'"));
	}

}
