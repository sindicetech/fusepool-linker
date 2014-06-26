package com.sindice.fusepooladapter;

import org.apache.clerezza.rdf.core.TripleCollection;

import com.sindice.fusepooladapter.configuration.DbpediaLinkerConfiguration;

import java.io.IOException;

/**
 * Concrete implementation of Intrelinker for deduplicate Dbpedia companies
 * 
 * @author szydan
 *
 */
public class DbpediaLinkerAdapter extends LinkerAdapter {

    @Override
	public TripleCollection interlink(TripleCollection dataToInterlink) {
		return interlink(dataToInterlink, DbpediaLinkerConfiguration.getInstance());
	}

	@Override
	public TripleCollection interlink(TripleCollection dataset1, TripleCollection dataset2) {
		throw new UnsupportedOperationException("Not supported. Use PatentsDbpediaLinkerAdapter");
	}

	@Override
	public String getName() {
		return "duke-interlinker "+ this.getClass().getName();
	}

}
