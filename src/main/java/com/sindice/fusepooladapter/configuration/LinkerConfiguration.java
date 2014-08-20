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

import no.priv.garshol.duke.ConfigLoader;
import no.priv.garshol.duke.Configuration;

import org.xml.sax.SAXException;

import com.sindice.fusepooladapter.LinkerAdapter;

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
