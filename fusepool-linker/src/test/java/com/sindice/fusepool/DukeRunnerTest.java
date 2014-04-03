/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.priv.garshol.duke.ConfigurationImpl;
import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.PropertyImpl;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.comparators.Levenshtein;

import org.junit.Before;
import org.junit.Test;

import com.sindice.fusepool.matchers.CollectingMatchListener;
import com.sindice.fusepool.stores.SetTripleWriter;

public class DukeRunnerTest extends DukeBaseTest {
	private ConfigurationImpl config;

	@Before
	public void setup() throws IOException {
		Levenshtein comp = new Levenshtein();
		List<Property> props = new ArrayList<Property>();
		props.add(new PropertyImpl("ID"));
		props.add(new PropertyImpl("NAME", comp, 0.3, 0.8));
		props.add(new PropertyImpl("EMAIL", comp, 0.3, 0.8));

		config = new ConfigurationImpl();
		config.setProperties(props);
		config.setThreshold(0.85);
		config.setMaybeThreshold(0.8);
	}

	@Test
	public void testSetup() {
		@SuppressWarnings("unused")
		DukeRunner duke = new DukeRunner("conf3.xml");
		duke = new DukeRunner("conf3.xml",
				new CollectingMatchListener(new SetTripleWriter()));
	}

	@Test
	public void testCollectingMatches() {
		SetTripleWriter matches = new SetTripleWriter();

	    Collection<Record> records = new ArrayList<Record>();
	    records.add(DukeBaseTest.makeRecord("ID", "1", "NAME", "aaaaa", "EMAIL", "BBBBB"));
	    records.add(DukeBaseTest.makeRecord("ID", "2", "NAME", "aaaaa", "EMAIL", "BBBBB"));

		config.addDataSource(0, new TestDataSource(records));
		DukeRunner duke = new DukeRunner(config);
		
		duke.addMatchListener(new CollectingMatchListener(matches));
			    
	    duke.run();
	    
	    assertEquals(2, matches.size());		
	}	
}
