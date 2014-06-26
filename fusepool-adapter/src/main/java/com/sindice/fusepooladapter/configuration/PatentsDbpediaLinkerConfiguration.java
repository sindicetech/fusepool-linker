package com.sindice.fusepooladapter.configuration;


/**
 *
 */
public class PatentsDbpediaLinkerConfiguration extends LinkerConfiguration {
    private static String patentsSparql =
              "PREFIX w3: <http://www.w3.org/ns/prov#> \n"
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

    private static String dbpediaSparql = "PREFIX foaf:        <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX rdfs:        <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX owl:         <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
            "PREFIX dbpedia:     <http://dbpedia.org/property/>\n" +
            "PREFIX sindicetech: <http://sindicetech.com/ontology/>\n" +
            "SELECT ?iri ?companyName ?countryCode ?locationCityName WHERE {" +
            "    ?iri a dbpedia-owl:Company .\n" +
            "    OPTIONAL { ?iri foaf:name ?companyName } \n" +
            "    OPTIONAL { ?iri dbpedia:countryCode ?countryCode }\n" +
            "    OPTIONAL { ?iri sindicetech:locationCityName ?locationCityName }\n" +
            "}";

    private static final PatentsDbpediaLinkerConfiguration instance = new PatentsDbpediaLinkerConfiguration();

    private PatentsDbpediaLinkerConfiguration() {
        super(loadConfig("classpath:dbpedia-csv.xml"), patentsSparql, dbpediaSparql);
    }

    public static LinkerConfiguration getInstance() {
        return instance;
    }
}
