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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsDbpediaLinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsLinkerConfiguration;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;

import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.sindice.fusepool.testutils.TestTripleCollectionPatents;

import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.Assert;

/**
 * Junit tests for manual testing of deduplication.
 * 
 * {@link #loadTestCollectionFull()} does the initial loading of the data later
 * used by {@link #testFull()}.
 * 
 * 
 */
public class LinkerAdapterTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Simple test to correctly generate property file
	 * @throws Exception
	 */
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
	 * A simple test of deduplication of 3 agents loaded from threeAgents.xml
	 * via {@link TestTripleCollectionPatents}. Two match, one doesn't. Uses the
	 * conf-final.xml Duke configuration specified in conf.properties.
	 */
	@Test
	public void testSmall() throws IOException {
	
		LinkerAdapter adapter = new GenericLinkerAdapter("conf.properties");
		TripleCollection resultTriples = adapter.interlink(new TestTripleCollectionPatents());
		assertEquals(0, resultTriples.size());
	}
	

	/**
	 * A simple test of deduplication of 3 agents loaded from threeAgents.xml
	 * via {@link TestTripleCollectionPatents}. Two match, one doesn't. Uses the
	 * conf-final.xml Duke configuration specified in conf.properties.
	 */
	@Test
	public void testSmallLinkerAdapter() throws IOException {
		LinkerAdapter adapter = new PatentLinkerAdapter();
		TripleCollection resultTriples = adapter.interlink(new TestTripleCollectionPatents());
		System.out.println(resultTriples.size());
	}
	
	
	@Test
	public void testSmallDataFile() throws IOException {
		LinkerAdapter adapter = new PatentLinkerAdapter();
		TripleCollection triples = Parser.getInstance().parse(getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
		TripleCollection resultTriples = adapter.interlink(triples);
		System.out.println("Found duplicates: " + resultTriples.size());
		Assert.assertTrue("no interlink found, but urn:x-temp:/id/5caaf04a-30ab-4c6a-9d0e-07ffc3a569d0"
				+ " and urn:x-temp:/id/5efdb576-ebb5-4efd-8b00-d943425eaf59 look the same", 
				resultTriples.size() > 0);
	}

    @Test
    public void testSmallDataInterlinkingFile() throws IOException {
        LinkerAdapter adapter = new PatentsDbpediaLinkerAdapter();
        TripleCollection patents = Parser.getInstance().parse(getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection companies = Parser.getInstance().parse(getClass().getResourceAsStream("companies500.nt"), SupportedFormat.N_TRIPLE);
        LinkerConfiguration configuration = new LinkerConfiguration(LinkerConfiguration.loadConfig("classpath:dbpedia-csv.xml"),
                PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery1(),
                PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery2());
        TripleCollection resultTriples = adapter.interlink(patents, companies, configuration);

        System.out.println("Found links between datasets: " + resultTriples.size());

        Assert.assertTrue("no interlink found, but the datasets transform to identical records",
                resultTriples.size() > 1);
    }

    @Test
    public void testPatentDbpediaInterlinkingSmall() throws IOException {
        LinkerAdapter adapter = new PatentsDbpediaLinkerAdapter();
        TripleCollection patents = Parser.getInstance().parse(getClass().getResourceAsStream("patent_5.ttl"), SupportedFormat.TURTLE);
        TripleCollection companies = Parser.getInstance().parse(getClass().getResourceAsStream("companies_5.nt"), SupportedFormat.N_TRIPLE);

        LinkerConfiguration configuration = new LinkerConfiguration(LinkerConfiguration.loadConfig("classpath:patentDbpediaSmall-csv.xml"),
                PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery1(),
                PatentsDbpediaLinkerConfiguration.getInstance().getSparqlQuery2());
        TripleCollection resultTriples = adapter.interlink(patents, companies, configuration);

        System.out.println("Found links between datasets: " + resultTriples.size());

        Assert.assertTrue("no interlink found, but the datasets transform to identical records",
                resultTriples.size() == 1);
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
	@Ignore
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

    /**
     *
     */
    @Ignore
    @Test
    public void testInterlinkingFull() throws IOException {
    	// here load the collections it is needed only once
    	loadTestCollectionFull("/Users/szydan/home/data/fusepool/patents/patent-data-sample.nt", "tmp/patentsJena", "N-TRIPLE");
    	loadTestCollectionFull("/Users/szydan/home/data/fusepool/dbpedia-companies/dbpedia-companies-small.nt", "tmp/dbpediaSmallJena", "N-TRIPLE");
    	
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

	/**
	 * A method to verify the number of triples in the "out" store populated by the full test.
	 * 
	 *  Commented code cleans the store.
	 */
	@Ignore
	@Test
	public void testVerify() throws IOException {
		JenaStoreTripleCollection os = new JenaStoreTripleCollection("tmp/out");
		os.init();
		System.out.println(os.size());
//		os.clean();
//		os.destroy();
	}

    @Test
    public void testList() throws IOException {
        JenaStoreTripleCollection os = new JenaStoreTripleCollection("/home/jakub/cvs/sindicetech/fusepool-linker/patentDbpediaOut");
        os.init();

        //  $ sed -r 's/^([^ ]*) .*/\1/' patentDbpediaOut.nt | sort -u | wc -l
        //  92

        BufferedWriter writer = Files.newBufferedWriter(Paths.get("tmp/patentDbpediaOut.nt"), Charset.forName("UTF-8"));
        for (Triple triple : os) {
            System.out.println(triple);
            writer.write(triple.toString());
            writer.newLine();
        }
    }

	/**
	 * Populates a Jena TDB store with the dataset that is to be deduplicated by the testFull() method.
	 *  
	 */
	
    @Ignore
	@Test
	public void loadTestCollectionFull() throws FileNotFoundException {
    	loadTestCollectionFull("/data/tmp_fusepool/dbpedia-companies/dbpedia-companies-small.nt", "tmp/dbpediaSmallJena", "N-TRIPLE");
    }	
    
    private void loadTestCollectionFull(String pathToInputFileInNtriplesFormat, String pathToDataset, String format) throws FileNotFoundException {
		File collection = new File(pathToDataset);
		collection.delete();
		
    	Dataset dataset = TDBFactory.createDataset(pathToDataset);
		dataset.begin(ReadWrite.WRITE);
		// Get model inside the transaction
		Model model = dataset.getDefaultModel();
		FileInputStream in = null;
		try {
			in = new FileInputStream(pathToInputFileInNtriplesFormat);
			logger.info(pathToInputFileInNtriplesFormat);
			model.read(in, null, format);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(model.size());
		dataset.commit();
		TDB.sync(dataset);
		dataset.end();
	}
}
