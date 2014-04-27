/*
 * Created by Sindice LTD http://sindicetech.com
 * Sindice LTD licenses this file to You under the Apache License, Version 2.0
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
package com.sindice.fusepooladapter.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
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

/**
 * 
 * implementation of
 * {@link com.sindice.fusepooladapter.storage.InputTripleStore } that uses Sesame
 * rdf framework
 * 
 * 
 */
public class SesameInputNativeStoreImpl implements InputTripleStore {
	private static final Logger logger = LoggerFactory
			.getLogger(SesameInputNativeStoreImpl.class);
	private final String datafolder;
	
	public static void main(String[] args) {
String query = "    PREFIX w3: <http://www.w3.org/ns/prov#> \n" +
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
    "PREFIX sumo: <http://www.owl-ontologies.com/sumo.owl#> \n" +
    "PREFIX schema: <http://schema.org/> \n" +
    "PREFIX pmo: <http://www.patexpert.org/ontologies/pmo.owl#> \n" +
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n" +

"SELECT * WHERE {  \n" +
"  ?agent a sumo:CognitiveAgent . \n" +
  "?agent rdfs:label ?agentName . \n" +
  "OPTIONAL {  \n" +
    "?agent schema:address  ?agentAddressUri . \n" +
    "OPTIONAL { ?agentAddressUri schema:addressCountry ?addressCountryUri .} \n" +
    "OPTIONAL { ?agentAddressUri schema:addressLocality ?addressLocality . } \n" +
    "OPTIONAL { ?agentAddressUri schema:streetAddress ?streetAddress .  }  \n" +
  "} \n" +
"} ORDER BY ?agent ";		
		
//query = "select * where {?s ?p ?o. }";

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
//			RepositoryResult<Statement> res = con.getStatements(null, null, null, false);
//			while (res.hasNext()) {
//				Statement st = res.next();
//				System.out.println("Statement: " + TripleWriter.toString(st));
//			}
			System.out.println("Size: " + con.size());
			
			TupleQueryResult result = null;
			try {
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);

			  result = tupleQuery.evaluate();
			  
				System.out.println("Has next X: " + result.hasNext());
			  while (result.hasNext()) {
				  BindingSet bs = result.next();
				  //System.out.println("Binding: " + bs.toString());
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

	public SesameInputNativeStoreImpl(String datafolder) {
		Path dataPath = Paths.get(datafolder);
		if (Files.exists(dataPath)) {
			if (!Files.isDirectory(dataPath)) {
				throw new IllegalArgumentException(
						"file instead of folder specified for output data");
			}
		}
		this.datafolder = datafolder;
	}

	/**
	 * cleans and populate the triplestore by triples from input collection
	 */
	@Override
	public int populate(TripleCollection triples) {
		SafeDeleter.delete(datafolder);

		logger.info("Creating Sesame Native store in " + datafolder);
		Repository repo = new SailRepository(new NativeStore(new File(
				datafolder)));
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
			//URI context
			try {
				Iterator<Triple> iterator = triples.iterator();
				while (iterator.hasNext()) {
					Triple triple = iterator.next();
					//System.out.println("triple: " + triple.toString());
//					URI s = factory.createURI(triple.getSubject().toString());
//					URI p = factory.createURI(triple.getPredicate().toString());
//					URI o = factory.createURI(triple.getObject().toString());
					//con.add(s, p, o);
					con.add(SesameUtils.toStatement(factory, triple));
				}

				size = con.size();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem working with Sesame Native store: " + e.getMessage(), e);
		}

		return (int) size; // TODO change interface
	}

	public void init() {
		// nothing to do in this class

	}

}
