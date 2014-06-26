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
package com.sindice.fusepool.matchers;

import java.io.Closeable;
import java.io.IOException;

import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.matchers.AbstractMatchListener;
import no.priv.garshol.duke.matchers.MatchListener;

import com.sindice.fusepool.stores.TripleStorageException;
import com.sindice.fusepool.stores.TripleWriter;

/**
 * Duke {@link MatchListener} to collect matches to a given collection.
 * 
 * If the passed collection implements {@link Closeable} then calls close() at
 * the end of processing.
 */
public class CollectingMatchListener extends AbstractMatchListener {
	static String ID_PROPERTY = "ID";
	private TripleWriter collection;

	/**
	 * Constructs a CollectingMatchListener for the given collection and
	 * converter.
	 * 
	 * @param collection
	 *            Collection to which to store the converted matches.
	 */
	public CollectingMatchListener(TripleWriter collection) {
		this.collection = collection;
	}

	@Override
	public void matches(Record r1, Record r2, double confidence) {
		String id1 = r1.getValue(ID_PROPERTY);
		String id2 = r2.getValue(ID_PROPERTY);

		collection.add(new SimpleTriple(id1, SimpleTriple.OWL_SAMEAS, id2));
	}

	@Override
	public void endProcessing() {
		if (collection instanceof AutoCloseable) {
			try {
				collection.close();
			} catch (Exception e) {
				throw new TripleStorageException(
						"Problem while closing writer: " + e.getMessage(), e);
			}
		}
	}

	public TripleWriter getCollection() {
		return collection;
	}
}
