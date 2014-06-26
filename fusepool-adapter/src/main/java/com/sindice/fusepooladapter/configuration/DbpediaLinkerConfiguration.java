package com.sindice.fusepooladapter.configuration;


public class DbpediaLinkerConfiguration extends LinkerConfiguration {

	public static final String DEFAULT_DEDUP_CONFIG_FILE_LOCATION = "classpath:dbpedia-csv.xml";  
	//TODO: create the config file the one here is used for interlinking 
	// we need another one with just one dataset for deduplication
	private static final DbpediaLinkerConfiguration instance = new DbpediaLinkerConfiguration();

	private DbpediaLinkerConfiguration() {
		//TODO: create the config file
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