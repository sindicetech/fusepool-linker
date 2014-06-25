package com.sindice.fusepool.cleaners;

import junit.framework.Assert;
import no.priv.garshol.duke.Cleaner;
import org.junit.Test;

/**
 *
 */
public class UriToCountryCodeCleanerTest {

    @Test
    public void testClean() throws Exception {
        Cleaner cleaner = new UriToCountryCodeCleaner();
        Assert.assertEquals("LU", cleaner.clean("urn:x-temp:/code/country/LU"));
    }
}
