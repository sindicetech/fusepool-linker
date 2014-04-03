/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
