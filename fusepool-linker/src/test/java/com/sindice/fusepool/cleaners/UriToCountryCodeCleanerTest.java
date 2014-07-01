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
