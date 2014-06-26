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
package com.sindice.fusepool;

import com.sindice.fusepool.matchers.CollectingMatchListener;
import com.sindice.fusepool.stores.TripleWriter;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.matchers.MatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Convenience class to run Duke deduplication in embedded mode.
 * 
 * Always creates a new Lucene index.
 * 
 */
public class DukeDeduplicatorRunner {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Configuration configuration;
	private Processor processor;

    /**
     *
     */
	public DukeDeduplicatorRunner(Configuration configuration, TripleWriter outputWriter, int dukeThreads) {
		this.configuration = configuration;
        configuration.validate();

        outputWriter.init();

		initialize(new CollectingMatchListener(outputWriter));
		
		processor.setThreads(dukeThreads);
	}	

    private void initialize(MatchListener... listeners) {
		this.processor = new Processor(configuration, true);
		for (MatchListener listener : listeners) {
			addMatchListener(listener);
		}
		this.processor.setLogger(new DukeSlf4jLogger(LoggerFactory.getLogger(Processor.class)));
	}
		
	public void addMatchListener(MatchListener listener) {
		processor.addMatchListener(listener);
	}
	
	public Collection<MatchListener> getMatchListeners() {
		return processor.getListeners();
	}
	
	public void run() {
        if (configuration.getDataSources().isEmpty()) {
            // datasources are probably configured in groups --> interlinking
            logger.info("Starting interlinking");
            processor.link();
        } else {
            logger.info("Starting deduplication");
            processor.deduplicate();
        }
	}
}


