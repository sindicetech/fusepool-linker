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
package com.sindice.fusepooladapter;

import java.io.IOException;
import java.util.Properties;

import no.priv.garshol.duke.Configuration;

import org.apache.clerezza.rdf.core.TripleCollection;

import com.sindice.fusepooladapter.configuration.LinkerConfiguration;

/**
 * Generic implementation of {@link LinkerAdapter} for
 * deduplication or interlinking of any 2 datasets.
 *
 * Deduplication and interlinking is done by Duke.
 * To use it one MUST provide a {@link LinkerConfiguration}.
 * 
 */
public class GenericLinkerAdapter extends LinkerAdapter {

	LinkerConfiguration linkerConfiguration;
	
	public GenericLinkerAdapter(LinkerConfiguration linkerConfiguration) {
		this.linkerConfiguration = linkerConfiguration;
	}

	public GenericLinkerAdapter(Configuration dukeConfiguration, String sparqlQuery1, String sparqlQuery2) {
		this.linkerConfiguration = new LinkerConfiguration(dukeConfiguration, sparqlQuery1, sparqlQuery2);
	}
	
	public GenericLinkerAdapter(Configuration dukeConfiguration, String sparqlQuery1, String sparqlQuery2, int threadsNo) throws IOException {
		this.dukeThreadNo  = threadsNo;
		this.linkerConfiguration = new LinkerConfiguration( dukeConfiguration, sparqlQuery1, sparqlQuery2);
	}
	
	public GenericLinkerAdapter(String dukeConfigurationPath, String sparqlQuery1, String sparqlQuery2, int threadsNo) throws IOException {
		this.dukeThreadNo  = threadsNo;
		this.linkerConfiguration = new LinkerConfiguration( LinkerConfiguration.loadConfig(dukeConfigurationPath), sparqlQuery1, sparqlQuery2);
	}

	public GenericLinkerAdapter(String confFileName) throws IOException {

		Properties properties = new Properties();
		properties.load(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(confFileName));
		if ( properties.getProperty("dukeconfig") == null || properties.getProperty("dukethrno") == null ) {
			throw new IllegalArgumentException("missing mandatory property");
		}
		this.dukeThreadNo = Integer.parseInt(properties.getProperty("dukethrno"));
		String dukeConfigurationPath = properties.getProperty("dukeconfig");
		
		//TODO: here if duke is configured for deduplication and sparqlDatasource is used 
		// then sparqlQueries are not needed at all -> remove the condition above 
		// but they are required if the datasource is csv
		// properties.getProperty("sparqlQuery1") == null
		String sparqlQuery1 = properties.getProperty("sparqlQuery1");
		String sparqlQuery2 = properties.getProperty("sparqlQuery2");
		this.linkerConfiguration = new LinkerConfiguration( LinkerConfiguration.loadConfig(dukeConfigurationPath), sparqlQuery1, sparqlQuery2);
	}
	
	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink) {
		return interlink(dataToInterlink, linkerConfiguration);
	}

	@Override
	public TripleCollection interlink(TripleCollection source, TripleCollection target) {
		return interlink(source, target,  linkerConfiguration);
	}

	@Override
	public String getName() {
		return "duke-interlinker "+ this.getClass().getName();
	}

}
