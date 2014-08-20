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
 * A concrete implementation of {@link  LinkerConfiguration}
 * Holds default configuration for deduplicating MAREC patents
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