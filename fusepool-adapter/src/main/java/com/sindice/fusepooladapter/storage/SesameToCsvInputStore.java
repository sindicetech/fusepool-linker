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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sindice.fusepool.DukeDeduplicatorRunner;
import com.sindice.fusepooladapter.LinkerAdapter;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
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

import com.sindice.fusepool.StopWatch;

/**
 * 
 * implementation of
 * {@link com.sindice.fusepooladapter.storage.InputTripleStore } that uses Sesame
 * rdf framework
 * 
 * 
 */
public class SesameToCsvInputStore implements InputTripleStore {
	private static final Logger logger = LoggerFactory
			.getLogger(SesameToCsvInputStore.class);
	public static final String query = "    PREFIX w3: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
			+ "PREFIX sumo: <http://www.owl-ontologies.com/sumo.owl#> \n"
			+ "PREFIX schema: <http://schema.org/> \n"
			+ "PREFIX pmo: <http://www.patexpert.org/ontologies/pmo.owl#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n"
			+

			"SELECT ?agent ?agentName ?addressUri ?addressCountryUri ?addressLocality ?streetAddress WHERE {  \n"
			+ "  ?agent a sumo:CognitiveAgent . \n"
			+ "?agent rdfs:label ?agentName . \n"
			+ "OPTIONAL {  \n"
			+ "?agent schema:address  ?addressUri . \n"
			+ "OPTIONAL { ?addressUri schema:addressCountry ?addressCountryUri .} \n"
			+ "OPTIONAL { ?addressUri schema:addressLocality ?addressLocality . } \n"
			+ "OPTIONAL { ?addressUri schema:streetAddress ?streetAddress .  }  \n"
			+ "} \n" + "} ORDER BY ?agent ";
    private final Writer writer;

    public static void main(String[] args) {

		// query = "select * where {?s ?p ?o. }";

		Repository repo = new SailRepository(new NativeStore(new File(
				"/tmp/1398437680141-0")));
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
			// RepositoryResult<Statement> res = con.getStatements(null, null,
			// null, false);
			// while (res.hasNext()) {
			// Statement st = res.next();
			// System.out.println("Statement: " + TripleWriter.toString(st));
			// }
			System.out.println("Size: " + con.size());

			TupleQueryResult result = null;
			try {
				TupleQuery tupleQuery = con.prepareTupleQuery(
						QueryLanguage.SPARQL, query);

				result = tupleQuery.evaluate();

				System.out.println("Has next X: " + result.hasNext());
				while (result.hasNext()) {
					BindingSet bs = result.next();
					// System.out.println("Binding: " + bs.toString());
				}
			} finally {
				if (result != null) {
					result.close();
				}
				con.close();
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public SesameToCsvInputStore(Writer writer) {
        this.writer = writer;
	}

	/**
	 * cleans and populate the triplestore by triples from input collection
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
				logger.info(StopWatch.popTimeString("Commiting took %s ms."));
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Problem working with Sesame Native store: "
							+ e.getMessage(), e);
		}

		return (int) size; // TODO change interface
	}

	public static String[] header = {
		"agent", "agentName", "addressUri", "addressCountry", "addressCountryUri", "addressLocality", "streetAddress"
	};
	private static CellProcessor[] processors = {
		new NotNull(), new NotNull(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional() 
	};

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
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					query);

			result = tupleQuery.evaluate();

			mapWriter = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            
            mapWriter.writeHeader(header);
			            			
			while (result.hasNext()) {
				BindingSet bs = result.next();	          
				
	            mapWriter.write(toMap(bs), header, processors);
			}
		} finally {
			if (result != null) {
				result.close();
			}
			mapWriter.close();
		}

	}

	public void init() {
		// nothing to do in this class

	}

}
