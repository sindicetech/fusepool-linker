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
