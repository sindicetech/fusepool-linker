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

import no.priv.garshol.duke.Cleaner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts URIs of the form urn:x-temp:/code/country/LU to LU
 *
 * For use with MAREC patent data when interlinking against DbPedia
 */
public class UriToCountryCodeCleaner implements Cleaner {
    private static final Pattern URI_PATTERN = Pattern.compile("^urn:x-temp:/code/country/");

    @Override
    public String clean(String value) {
        Matcher matcher = URI_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.replaceFirst("");
        }
        return value;
    }
}
