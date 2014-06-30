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
        PatentLinkerAdapter adapter = new PatentLinkerAdapter();
        TripleCollection triples = Parser.getInstance().parse(getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection resultTriples = adapter.interlink(triples);
        Assert.assertTrue("No interlink found, but for example urn:x-temp:/id/21bb523d-390a-4713-9026-06c37f78881c"
                + " and urn:x-temp:/id/2504abd0-7ee7-45ef-88df-ad318ec33869 should match",
                resultTriples.size() > 0);
    }

    /*
    a modification of testSmallDataFile() for debugging
     */
    @Test
    public void testSmallDataFileWithDebugging() throws IOException {
        DebuggingLinkerAdapter adapter = new DebuggingLinkerAdapter(PatentsLinkerConfiguration.getInstance()); //new PatentLinkerAdapter();
        TripleCollection triples = Parser.getInstance().parse(getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection resultTriples = adapter.interlink(triples);
        Assert.assertTrue("No interlink found, but for example urn:x-temp:/id/21bb523d-390a-4713-9026-06c37f78881c"
                + " and urn:x-temp:/id/2504abd0-7ee7-45ef-88df-ad318ec33869 should match",
                resultTriples.size() > 0);
        adapter.analyze(2);
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
