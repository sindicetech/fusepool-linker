package com.sindice.fusepooladapter;

import com.sindice.fusepool.testutils.TestTripleCollectionPatents;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsDbpediaLinkerConfiguration;
import com.sindice.fusepooladapter.configuration.PatentsLinkerConfiguration;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LinkerAdapterTest {
    /**
     * A simple test of deduplication of 3 agents loaded from threeAgents.xml
     * via {@link TestTripleCollectionPatents}. Two match, one doesn't. Uses the
     * conf-final.xml Duke configuration specified in conf.properties.
     */
    @Test
    public void testSmall() throws IOException {

        LinkerAdapter adapter = new GenericLinkerAdapter("conf.properties");
        TripleCollection resultTriples = adapter.interlink(new TestTripleCollectionPatents());
        assertEquals(2, resultTriples.size());
    }

    @Test
    public void testSmallDataFile() throws IOException {
        LinkerAdapter adapter = new DebuggingLinkerAdapter(); //new PatentLinkerAdapter();
        TripleCollection triples = Parser.getInstance().parse(getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection resultTriples = adapter.interlink(triples, PatentsLinkerConfiguration.getInstance());
        Assert.assertTrue("No interlink found, but urn:x-temp:/id/5caaf04a-30ab-4c6a-9d0e-07ffc3a569d0"
                + " and urn:x-temp:/id/5efdb576-ebb5-4efd-8b00-d943425eaf59 should match",
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

        Assert.assertTrue("No interlink found, but the datasets transform to identical records",
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

        Assert.assertTrue("No interlink found, but the datasets transform to identical records",
                resultTriples.size() == 1);
    }
}
