/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.cleaners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.priv.garshol.duke.Cleaner;

public class NameCleaner implements Cleaner {
	private static final Pattern DELETED = Pattern.compile("^DELETED ");
	private static final Pattern STATE = Pattern.compile("\\s\\([A-Z]{2}\\).*");
	private static final Pattern R102 = Pattern.compile("\\sR\\.\\s102.*\\d{2}\\.\\d{2}\\.\\d{4}$");
	private static final Pattern DECEASED = Pattern.compile("\\s\\(?deceased\\)?.*\\d{2}\\.\\d{2}\\.\\d{4}$");
	public String clean(String input) {
		String output = input;
		Matcher matcher = DELETED.matcher(input);
		if (matcher.find()){
			output =matcher.replaceFirst("");
		}
		
		matcher = STATE.matcher(output);
		//there are only mess behind a state
		if (matcher.find()){
			return matcher.replaceFirst("");
		}
		matcher = R102.matcher(output);
		if (matcher.find()){
			return matcher.replaceFirst("");
		}
		matcher = DECEASED.matcher(output);
		if (matcher.find()){
			return matcher.replaceFirst("");
		}
		return output;
	}

}
