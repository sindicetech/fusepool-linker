package com.sindice.fusepooladapter;

import no.priv.garshol.duke.ConfigLoader;
import no.priv.garshol.duke.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * LinkerAdapter dukeConfiguration holder.
 *
 * sparqlQuery1 corresponds to the first argument of {@link LinkerAdapter#interlink(org.apache.clerezza.rdf.core.TripleCollection, org.apache.clerezza.rdf.core.TripleCollection)}
 * sparqlQuery2 corresponds to the second argument of {@link LinkerAdapter#interlink(org.apache.clerezza.rdf.core.TripleCollection, org.apache.clerezza.rdf.core.TripleCollection)}
 */
public class LinkerConfiguration {
    private Configuration dukeConfiguration;
    private String sparqlQuery1, sparqlQuery2;

    public LinkerConfiguration(Configuration dukeConfiguration, String sparqlQuery1, String sparqlQuery2) {
        this.dukeConfiguration = dukeConfiguration;
        this.sparqlQuery1 = sparqlQuery1;
        this.sparqlQuery2 = sparqlQuery2;
    }

    public static Configuration loadConfig(String pathToConfig) {
        Thread.currentThread().setContextClassLoader(LinkerConfiguration.class.getClassLoader());   // because Fusepool
        try {
            try {
                return ConfigLoader.load(pathToConfig);
            } catch (IOException e) {
                if (!pathToConfig.startsWith("classpath:")) {
                    try {
                        return ConfigLoader.load("classpath:" + pathToConfig);
                    } catch (IOException e1) {
                        throw new RuntimeException("Problem accessing Duke dukeConfiguration: " + pathToConfig, e1);
                    }
                }
                throw new RuntimeException("Problem accessing Duke dukeConfiguration: " + pathToConfig);
            }
        } catch (SAXException e) {
            throw new RuntimeException("Problem while reading Duke dukeConfiguration " + pathToConfig, e);
        }
    }

    public Configuration getDukeConfiguration() {
        return dukeConfiguration;
    }

    public void setDukeConfiguration(Configuration dukeConfiguration) {
        this.dukeConfiguration = dukeConfiguration;
    }

    public String getSparqlQuery1() {
        return sparqlQuery1;
    }

    public void setSparqlQuery1(String sparqlQuery1) {
        this.sparqlQuery1 = sparqlQuery1;
    }

    public String getSparqlQuery2() {
        return sparqlQuery2;
    }

    public void setSparqlQuery2(String sparqlQuery2) {
        this.sparqlQuery2 = sparqlQuery2;
    }
}
