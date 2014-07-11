/* 
 * Copyright 2014 Sindice LTD http://sindicetech.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sindice.fusepool.cleaners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.priv.garshol.duke.Cleaner;

/**
 * Name cleaner for MAREC patent data.
 */
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
