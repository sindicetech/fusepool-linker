/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.testutils;

import java.util.Collection;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.serializedform.Parser;
/**
 * Immutable test instance for tests
 *
 */

public class TestTripleCollectionPatents implements TripleCollection {
	private Graph testGraph;
	
	
	public  TestTripleCollectionPatents(){
		final Parser parser = Parser.getInstance();
		testGraph = parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("threeAgents.xml"), "application/rdf+xml");
		
	}
	
	public boolean add(Triple arg0) {
		return false;
	}

	public boolean addAll(Collection<? extends Triple> arg0) {
		return false;
	}

	public void clear() {
		throw new UnsupportedOperationException();

	}

	public boolean contains(Object c) {
		testGraph.contains(c);
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		testGraph.containsAll(c);
		return false;
	}

	public boolean isEmpty() {
		return testGraph.isEmpty();
	}

	public Iterator<Triple> iterator() {
		return testGraph.iterator();
	}

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		return testGraph.size();
	}

	public Object[] toArray() {
		return testGraph.toArray();
	}

	public <T> T[] toArray(T[] arg0) {
		return testGraph.toArray(arg0);
	}

	public Iterator<Triple> filter(NonLiteral subject, UriRef predicate,
			Resource object) {
		return testGraph.filter(subject, predicate, object);
	}

	public void addGraphListener(GraphListener listener, FilterTriple filter,
			long delay) {
		testGraph.addGraphListener(listener, filter);

	}

	public void addGraphListener(GraphListener listener, FilterTriple filter) {
		testGraph.addGraphListener(listener, filter);

	}

	public void removeGraphListener(GraphListener listener) {
		testGraph.removeGraphListener(listener);

	}

}
