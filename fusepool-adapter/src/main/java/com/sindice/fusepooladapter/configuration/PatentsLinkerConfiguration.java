package com.sindice.fusepooladapter.configuration;


 /**
 * Concrete implementation of LinkerConfiguration
 * Holds default configuration for deduplicating Patents (marc)
 *
 * @author szydan
 *
 */
public class PatentsLinkerConfiguration extends LinkerConfiguration {

	public static final String DEFAULT_DEDUP_CONFIG_FILE_LOCATION = "classpath:patents-csv.xml";

	private static final PatentsLinkerConfiguration instance = new PatentsLinkerConfiguration();

	private PatentsLinkerConfiguration() {
		super(
			loadConfig(DEFAULT_DEDUP_CONFIG_FILE_LOCATION),
			PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery1(), // for patents use first query
			null
			);
	}

	public static LinkerConfiguration getInstance() {
		return instance;
	}
}