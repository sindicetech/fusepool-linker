package com.sindice.fusepooladapter;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.osgi.service.component.annotations.Component;

import com.sindice.fusepooladapter.configuration.PatentsDbpediaLinkerConfiguration;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * A concrete implementation of the {@link LinkerAdapter} for interlinking of MAREC patents with DbPedia companies.
 */
@Component(service = Interlinker.class)
public class PatentsDbpediaLinkerAdapter extends LinkerAdapter {

	@Override
	public TripleCollection interlink(TripleCollection source, TripleCollection target) {
        return interlink(source, target, PatentsDbpediaLinkerConfiguration.getInstance());
	}

	@Override
	public TripleCollection interlink(TripleCollection dataToInterlink) {
		throw new UnsupportedOperationException("Not supported. Use PatentsLinkerAdapter or DbpediaLinkerAdapter");
	}

	@Override
	public String getName() {
		return "duke-interlinker "+ this.getClass().getName();
	}

}
