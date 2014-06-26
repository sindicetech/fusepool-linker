package com.sindice.fusepooladapter;

import java.io.IOException;
import java.util.Properties;

import no.priv.garshol.duke.Configuration;

import org.apache.clerezza.rdf.core.TripleCollection;

import com.sindice.fusepooladapter.configuration.LinkerConfiguration;

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
		if ( properties.getProperty("dukeconfig") == null || properties.getProperty("dukethrno") == null || properties.getProperty("sparqlQuery1") == null) {
			throw new IllegalArgumentException("missing mandatory property");
		}
		this.dukeThreadNo = Integer.parseInt(properties.getProperty("dukethrno"));
		String dukeConfigurationPath = properties.getProperty("dukeconfig");
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
