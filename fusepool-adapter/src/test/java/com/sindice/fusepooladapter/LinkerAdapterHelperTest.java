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
package com.sindice.fusepooladapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindice.fusepool.testutils.TestTripleCollectionPatents;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsDbpediaLinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsLinkerConfiguration;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;
import com.sindice.fusepooladapter.tools.StorageHelper;

/**
 * Junit tests for manual testing of deduplication.
 * 
 */
public class LinkerAdapterHelperTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	
	/**
	 * Simple test to correctly generate property file
	 * @throws Exception
	 */
    @Ignore
	@Test
	public void saveParamChanges() throws Exception {

	        File dir = new File("target");
	        dir.mkdir();
			Properties props = new Properties();
	        props.setProperty("sparqlQuery1", PatentsLinkerConfiguration.getInstance().getSparqlQuery1());
	        props.setProperty("sparqlQuery2", PatentsLinkerConfiguration.getInstance().getSparqlQuery2());
	        
	        File f = new File(dir,"test.properties");
	        
	        OutputStream out = new FileOutputStream( f );
	        props.store(out, "This is an optional header comment string");
	}

    /**
	 * Test that runs the full flow. 
	 * 
	 *  It is assumed that the "fullTestData" is a Jena TDB data folder loaded with the input dataset to deduplicate.
	 *  This store plays the role of the store that in reality backs the real Clerezza TripleCollection. 
	 *  
	 *  Output is stored to the specified outpath - another Jena TDB store.
	 * 
	 */
	//@Ignore
	@Test
	public void testDeduplicationFull() throws IOException {
		
		// load the collection has to be done only once
		//loadTestCollectionFull("/Users/szydan/home/data/fusepool/patents/patent-data-sample.nt", "tmp/patentsJena", "N-TRIPLE");
    	
		logger.info("Full test");
		
		LinkerAdapter adapter = new PatentLinkerAdapter();
		adapter.setDukeThreadNo(Runtime.getRuntime().availableProcessors());
        
		JenaStoreTripleCollection os = new JenaStoreTripleCollection("tmp/patentsJena"); // contains the dataset to deduplicate. Can be loaded using loadTestCollectionFull()
		os.init();
		logger.info("Store contains " + os.size() + " triples.");
		TripleCollection resultTriples = adapter.interlink(os);
		logger.info("Triples in result: " + resultTriples.size());
		os.destroy();
	}
	
    @Ignore
	@Test
    public void testDeduplicationFullPatentsSparql() throws IOException {

        LinkerAdapter adapter = new GenericLinkerAdapter("sparql-patent-deduplication.properties");
        adapter.setDukeThreadNo(Runtime.getRuntime().availableProcessors());
        
        // triple collection can be null as it will not be used at the moment  
        TripleCollection resultTriples = adapter.interlink(new TestTripleCollectionPatents());
		logger.info("Triples in result: " + resultTriples.size());
    }

    @Ignore
	@Test
    public void testInterlinkingFullSparql() throws IOException {

        LinkerAdapter adapter = new GenericLinkerAdapter("sparql-patent-dbpedia-interlinking.properties");
        adapter.setDukeThreadNo(Runtime.getRuntime().availableProcessors());
        
        // triple collection can be null as it will not be used at the moment  
        TripleCollection resultTriples = adapter.interlink(new TestTripleCollectionPatents(), new TestTripleCollectionPatents());
		logger.info("Triples in result: " + resultTriples.size());
    }

    
    /**
     *
     */
    @Ignore
    @Test
    public void testInterlinkingFull() throws IOException {
    	// here load the collections it is needed only once
    	StorageHelper.loadTestCollectionFull("/Users/szydan/home/data/fusepool/patents/patent-data-sample.nt", "tmp/patentsJena", "N-TRIPLE");
        StorageHelper.loadTestCollectionFull("/Users/szydan/home/data/fusepool/dbpedia-companies/dbpedia-companies-small.nt", "tmp/dbpediaSmallJena", "N-TRIPLE");
    	
    	logger.info("Full test");
 
    	JenaStoreTripleCollection patents = new JenaStoreTripleCollection("tmp/patentsJena"); // contains the dataset to deduplicate. Can be loaded using loadTestCollectionFull()
        patents.init();
        JenaStoreTripleCollection dbpedia = new JenaStoreTripleCollection("tmp/dbpediaSmallJena"); // contains the dataset to deduplicate. Can be loaded using loadTestCollectionFull()
        dbpedia.init();
        logger.info("Patent store contains " + patents.size() + " triples.");
        logger.info("Dbpedia store contains " + dbpedia.size() + " triples.");

        LinkerAdapter adapter = new PatentsDbpediaLinkerAdapter();
        adapter.setDukeThreadNo(Runtime.getRuntime().availableProcessors());
        
        LinkerConfiguration linkerConfiguration = PatentsDbpediaLinkerConfiguration.getInstance();
        TripleCollection resultTriples = adapter.interlink(patents, dbpedia, linkerConfiguration );

        logger.info("Triples in result: " + resultTriples.size());
        patents.destroy();
        dbpedia.destroy();
    }

	@Ignore
	@Test
	public void testVerify() throws IOException {
        System.out.println(StorageHelper.getJenaTripleCollectionSize("tmp/out"));
	}

    //  $ sed -r 's/^([^ ]*) .*/\1/' patentDbpediaOut.nt | sort -u | wc -l
    //  small: 92   bigger: 159
    @Ignore
    @Test
    public void testList() throws IOException {
        StorageHelper.printJenaTripleCollectionToFile("/tmp/fusepool-linker-1F4DAB/adapter-2835908000707029049/output-6188574859386265433", "tmp/patentDbpediaOut.nt");
        //StorageHelper.printJenaTripleCollection("/tmp/fusepool-linker-1F4DAB/adapter-2835908000707029049/output-6188574859386265433");
    }

    @Ignore
    @Test
    public void compareRecords() {
//        DebuggingLinkerAdapter.compare(
//                "/tmp/fusepool-linker-1F4DAB/adapter-4335165401058607631/lucene-interlink-2103959415636551051",
//                PatentsLinkerConfiguration.getInstance().getDukeConfiguration(),
//                "urn:x-temp:/id/21bb523d-390a-4713-9026-06c37f78881c", "urn:x-temp:/id/2504abd0-7ee7-45ef-88df-ad318ec33869");
//        DebuggingLinkerAdapter.analyze("/tmp/fusepool-linker-1F4DAB/adapter-4335165401058607631/lucene-interlink-2103959415636551051",
//                PatentsLinkerConfiguration.getInstance().getDukeConfiguration(),
//                5);
        DebuggingLinkerAdapter.analyze("/tmp/fusepool-linker-1F4DAB/adapter-3930029187941568328/lucene-interlink-7074456329884731500", PatentsLinkerConfiguration.getInstance().getDukeConfiguration(),
                5);
    }
}
