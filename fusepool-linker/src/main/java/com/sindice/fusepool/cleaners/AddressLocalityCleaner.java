/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.cleaners;

import java.util.regex.Pattern;

import no.priv.garshol.duke.Cleaner;

public class AddressLocalityCleaner implements Cleaner {
	private static final Pattern STATE= Pattern.compile("^[A-Z]{1,2}\\-");

	public String clean(String input) {
		return STATE.matcher(input).replaceFirst("");
	}

}
