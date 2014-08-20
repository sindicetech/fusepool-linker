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
package com.sindice.fusepool.matchers;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.priv.garshol.duke.ConfigurationImpl;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.PropertyImpl;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.comparators.Levenshtein;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sindice.fusepool.DukeBaseTest;
import com.sindice.fusepool.stores.SetTripleWriter;

public class CollectingMatchListenerTest {
  private ConfigurationImpl config;
  private Processor processor;
  
  @Before
  public void setup() throws IOException {
    Levenshtein comp = new Levenshtein();
    List<Property> props = new ArrayList();
    props.add(new PropertyImpl("ID"));
    props.add(new PropertyImpl("NAME", comp, 0.3, 0.8));
    props.add(new PropertyImpl("EMAIL", comp, 0.3, 0.8));

    config = new ConfigurationImpl();
    config.setProperties(props);
    config.setThreshold(0.85);
    config.setMaybeThreshold(0.8);
    processor = new Processor(config, true);
  }

  @After
  public void cleanup() throws IOException {
    processor.close();
  }
  
  @Test
  public void testEmpty() throws IOException {
	SetTripleWriter matches = new SetTripleWriter();
	processor.addMatchListener(new CollectingMatchListener(matches));
    processor.deduplicate(new ArrayList());
    assertEquals(0, matches.size());
  }
  
  @Test
  public void testMatchesList() throws IOException {
	SetTripleWriter matches = new SetTripleWriter();
	processor.addMatchListener(new CollectingMatchListener(matches));
	
    Collection<Record> records = new ArrayList();
    records.add(DukeBaseTest.makeRecord("ID", "1", "NAME", "aaaaa", "EMAIL", "BBBBB"));
    records.add(DukeBaseTest.makeRecord("ID", "2", "NAME", "aaaaa", "EMAIL", "BBBBB"));
    processor.deduplicate(records);
    
    assertEquals(2, matches.size());
  }

  @Test
  public void testMatchesTripleCollection() throws IOException {
	SetTripleWriter matches = new SetTripleWriter();
	processor.addMatchListener(new CollectingMatchListener(matches));
	
    Collection<Record> records = new ArrayList();
    records.add(DukeBaseTest.makeRecord("ID", "1", "NAME", "aaaaa", "EMAIL", "BBBBB"));
    records.add(DukeBaseTest.makeRecord("ID", "2", "NAME", "aaaaa", "EMAIL", "BBBBB"));
    processor.deduplicate(records);
    
    assertEquals(2, matches.size());
  }    
}