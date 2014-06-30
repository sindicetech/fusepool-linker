package com.sindice.fusepooladapter.configuration;

/**
 *
* A concrete implementation of {@link LinkerConfiguration}
* Holds default configuration for deduplicating DbPedia companies
*
*/
public class DbpediaLinkerConfiguration extends LinkerConfiguration {

	public static final String DEFAULT_DEDUP_CONFIG_FILE_LOCATION = "classpath:dedup-dbpedia-csv.xml";  
	private static final DbpediaLinkerConfiguration instance = new DbpediaLinkerConfiguration();

	private DbpediaLinkerConfiguration() {
		super(
			loadConfig(DEFAULT_DEDUP_CONFIG_FILE_LOCATION),
			PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery2(),  // for dbpedia use second query
			null
		);
	}

	public static LinkerConfiguration getInstance() {
		return instance;
	}
}