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