package com.sindice.fusepooladapter;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.osgi.service.component.annotations.Component;

import com.sindice.fusepooladapter.configuration.PatentsLinkerConfiguration;

import eu.fusepool.datalifecycle.Interlinker;

/**
 * A concrete implementation of {@link LinkerAdapter} for deduplication of the MAREC patents dataset
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
        if (dataset1 == dataset2) {
            return interlink(dataset1);
        }
		throw new UnsupportedOperationException("Not supported. Use PatentsDbpediaLinkerAdapter");
	}

	@Override
	public String getName() {
		return "duke-interlinker "+ this.getClass().getName();
	}

}
