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
package com.sindice.fusepooladapter.storage;

import com.sindice.fusepool.StopWatch;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An implementation of {@link InputTripleStore } that uses the Sesame RDF framework.
 *
 */
public class ConfigurableSesameToCsvInputStore implements InputTripleStore {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurableSesameToCsvInputStore.class);
	private final String query;
    private final Writer writer;
    private final CsvConfig config;

    public ConfigurableSesameToCsvInputStore(Writer writer, String query) {
        this.writer = writer;
        this.query = query;
        this.config = SparqlToCsvHeader.transform(query);
    }

	/**
	 * Cleans and populates the triplestore by triples from input collection
	 */
	@Override
	public long populate(TripleCollection triples) {
        File tmpDir = com.google.common.io.Files.createTempDir();

		logger.info("Creating Sesame Native store in " + tmpDir);

		Repository repo = new SailRepository(new NativeStore(tmpDir));
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			throw new RuntimeException(
					"Problem initializing Sesame native store: "
							+ e.getMessage(), e);
		}

		long size = -1;

		try {
			RepositoryConnection con = repo.getConnection();
			ValueFactory factory = repo.getValueFactory();
			con.begin();
			// URI context
			StopWatch.start();
			try {
				Iterator<Triple> iterator = triples.iterator();
				while (iterator.hasNext()) {
					Triple triple = iterator.next();
					// System.out.println("triple: " + triple.toString());
					// URI s =
					// factory.createURI(triple.getSubject().toString());
					// URI p =
					// factory.createURI(triple.getPredicate().toString());
					// URI o = factory.createURI(triple.getObject().toString());
					// con.add(s, p, o);
					con.add(SesameUtils.toStatement(factory, triple));
				}

				size = con.size();
				StopWatch.end();
				
				logger.info(StopWatch.popTimeString("Loading Sesame store took %s ms without commit."));
				
				StopWatch.start();
				writeCsv(con);
				StopWatch.end();
				logger.info(StopWatch.popTimeString("Writing CSV took %s ms."));
			} finally {
				StopWatch.start();
				con.commit();
				con.close();
				StopWatch.end();
				logger.info(StopWatch.popTimeString("Committing took %s ms."));
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Problem working with Sesame Native store: "
							+ e.getMessage(), e);
		}

        //SafeDeleter.delete(tmpDir.getAbsolutePath());

		return size; // TODO change interface
	}

	private Map<String, Object> toMap(BindingSet set) {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<Binding> it = set.iterator();

		while (it.hasNext()) {
			Binding b = it.next();

			map.put(b.getName(), b.getValue().stringValue());
		}

		return map;
	}

	private void writeCsv(RepositoryConnection con) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException, IOException {
		TupleQueryResult result = null;
		ICsvMapWriter mapWriter = null;
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);

			result = tupleQuery.evaluate();

			mapWriter = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            
            mapWriter.writeHeader(config.getHeader());
			            			
			while (result.hasNext()) {
				BindingSet bs = result.next();	          
				
	            mapWriter.write(toMap(bs), config.getHeader(), config.getProcessors());
			}
		} finally {
			if (result != null) {
				result.close();
			}
            if (mapWriter != null) {
			    mapWriter.close();
            }
		}

	}

    @Override
	public void init() {
		// nothing to do in this class
	}
}
