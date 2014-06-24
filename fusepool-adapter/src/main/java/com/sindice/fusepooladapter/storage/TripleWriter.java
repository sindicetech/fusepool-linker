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
package com.sindice.fusepooladapter.storage;

import java.io.StringWriter;

import org.apache.clerezza.rdf.core.Triple;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * Helper for converting Sesame {@link Statement}s to {@link String}s.
 * 
 * The Sesame API makes it cumbersome otherwise.
 * 
 */
public class TripleWriter {
	static private StringWriter stringWriter = new StringWriter();
	static private NTriplesWriter ntWriter = new NTriplesWriter(stringWriter);
	static private ValueFactory factory = ValueFactoryImpl.getInstance();

	public static String toString(Statement statement)
			throws RDFHandlerException {
		stringWriter.getBuffer().setLength(0); // clear the string writer
		ntWriter.startRDF();
		ntWriter.handleStatement(statement);
		ntWriter.endRDF();

		String ntriple = stringWriter.toString();

		// NtriplesWriter adds a newline and so apparently does the RecordWriter
		// --> we have to strip it here
		return ntriple.substring(0, ntriple.length() - 1);
	}

	public static String toString(Triple triple) throws RDFHandlerException {
		return toString(factory.createStatement(
				factory.createURI(triple.getSubject().toString()),
				factory.createURI(triple.getPredicate().toString()), 
				factory.createURI(triple.getObject().toString())) );
	}
}