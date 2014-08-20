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
package com.sindice.fusepooladapter.storage;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.supercsv.cellprocessor.Optional;

import java.util.Arrays;

/**
 *
 */
public class SparqlToCsvHeaderTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    static String agentsQuery = "    PREFIX w3: <http://www.w3.org/ns/prov#> \n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX sumo: <http://www.owl-ontologies.com/sumo.owl#> \n"
            + "PREFIX schema: <http://schema.org/> \n"
            + "PREFIX pmo: <http://www.patexpert.org/ontologies/pmo.owl#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n"
            +

            "SELECT ?agent ?agentName ?addressUri ?addressCountryUri ?addressLocality ?streetAddress WHERE {  \n"
            + "  ?agent a sumo:CognitiveAgent . \n"
            + "?agent rdfs:label ?agentName . \n"
            + "OPTIONAL {  \n"
            + "?agent schema:address  ?addressUri . \n"
            + "OPTIONAL { ?addressUri schema:addressCountry ?addressCountryUri .} \n"
            + "OPTIONAL { ?addressUri schema:addressLocality ?addressLocality . } \n"
            + "OPTIONAL { ?addressUri schema:streetAddress ?streetAddress .  }  \n"
            + "} \n" + "} ORDER BY ?agent ";

    @Test
    public void testValidQuery() {
        CsvConfig config = SparqlToCsvHeader.transform(agentsQuery);
        Assert.assertArrayEquals(new String[] {"agent", "agentName", "addressUri", "addressCountryUri", "addressLocality", "streetAddress"}, config.getHeader());
        Assert.assertEquals(6, config.getProcessors().length);
    }

    @Test
    public void testStarQuery() {
        exception.expect(IllegalArgumentException.class);
        CsvConfig config = SparqlToCsvHeader.transform("SELECT * WHERE");
    }

    @Test
    public void testCaseInsensitive() {
        CsvConfig config = SparqlToCsvHeader.transform("select ?agent WHERE");
        Assert.assertArrayEquals(new String[]{"agent"}, config.getHeader());
    }

}
