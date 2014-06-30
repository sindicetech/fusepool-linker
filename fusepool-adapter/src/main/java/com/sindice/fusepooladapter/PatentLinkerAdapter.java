package com.sindice.fusepooladapter;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.osgi.service.component.annotations.Component;

import com.sindice.fusepooladapter.configuration.PatentsLinkerConfiguration;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * Concrete implementation of Intrelinker for deduplicate marks patents dataset
 * 
 * @author szydan
 *
 */
@Component(service = Interlinker.class)
public class PatentLinkerAdapter extends LinkerAdapter{

    @Override
	public TripleCollection interlink(TripleCollection dataToInterlink) {
		return interlink(dataToInterlink, PatentsLinkerConfiguration.getInstance());
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
