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

import java.util.regex.Pattern;

import no.priv.garshol.duke.Cleaner;

/**
 * Address cleaner for the MAREC patent data
 */
public class AddressLocalityCleaner implements Cleaner {
	private static final Pattern STATE= Pattern.compile("^[A-Z]{1,2}\\-");

	public String clean(String input) {
		return STATE.matcher(input).replaceFirst("");
	}

}
