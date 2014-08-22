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
