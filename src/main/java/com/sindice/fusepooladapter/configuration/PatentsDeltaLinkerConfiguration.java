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
package com.sindice.fusepooladapter.configuration;


/**
 * A concrete implementation of {@link com.sindice.fusepooladapter.configuration.LinkerConfiguration}
 * Holds default configuration for interlinking MAREC patents with DbPedia companies
 *
 */
public class PatentsDeltaLinkerConfiguration extends LinkerConfiguration {
  public static final String CONFIG_FILE = "classpath:dbpedia-delta-csv.xml";
  private static final String patentsSparql =
              "PREFIX w3: <http://www.w3.org/ns/prov#> \n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX sumo: <http://www.owl-ontologies.com/sumo.owl#> \n"
            + "PREFIX schema: <http://schema.org/> \n"
            + "PREFIX pmo: <http://www.patexpert.org/ontologies/pmo.owl#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n"
            +

            "SELECT ?agent ?agentName ?addressUri ?addressCountryUri ?addressLocality ?streetAddress WHERE {  \n"
            + "  ?agent a sumo:CognitiveAgent ; \n"
            + "         rdfs:label ?agentName . \n"
            + "OPTIONAL {  \n"
            + "?agent schema:address  ?addressUri . \n"
            + "OPTIONAL { ?addressUri schema:addressCountry ?addressCountryUri .} \n"
            + "OPTIONAL { ?addressUri schema:addressLocality ?addressLocality . } \n"
            + "OPTIONAL { ?addressUri schema:streetAddress ?streetAddress .  }  \n"
            + "} \n" + "} ORDER BY ?agent ";

    private static final PatentsDeltaLinkerConfiguration instance = new PatentsDeltaLinkerConfiguration();

    private PatentsDeltaLinkerConfiguration() {
        super(loadConfig(CONFIG_FILE), patentsSparql, patentsSparql);
    }

    public static LinkerConfiguration getInstance() {
        return instance;
    }
}
