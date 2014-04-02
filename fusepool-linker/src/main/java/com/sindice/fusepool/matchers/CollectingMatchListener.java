/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
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
 * 
 * 
 * @param <T>
 *            A triple type that is the result of converting a
 *            {@link SimpleTriple} via the given {@link TripleConverter}.
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
	 * @param converter
	 *            Converter to convert matches in the form of a
	 *            {@link SimpleTriple} to the resulting type T.
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
		if (collection instanceof Closeable) {
			try {
				((Closeable) collection).close();
			} catch (IOException e) {
				throw new TripleStorageException(
						"Problem while trying to commit: " + e.getMessage(), e);
			}
		}
	}

	public TripleWriter getCollection() {
		return collection;
	}
}
