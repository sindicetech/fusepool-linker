package com.sindice.fusepooladapter.storage;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

public class SesameUtils {
	public static Statement toStatement(ValueFactory factory, Triple triple) {
		Resource subject = null;
		URI predicate = null;
		Value object = null;
		
		NonLiteral s = triple.getSubject();
		if (s instanceof UriRef) {
			subject = factory.createURI(((UriRef) s).getUnicodeString());
		} else if (s instanceof BNode) {
			//TODO fix: proper b-node generating code
			subject = factory.createBNode(s.toString());
		}
		
		predicate = factory.createURI(triple.getPredicate().getUnicodeString());
		
		org.apache.clerezza.rdf.core.Resource o = triple.getObject();
		if (o instanceof UriRef) {
			object = factory.createURI(((UriRef) o).getUnicodeString());
		} else if (o instanceof BNode) {
			object = factory.createBNode(o.toString());
		} else if (o instanceof PlainLiteral) {
			object = factory.createLiteral(((PlainLiteral) o).getLexicalForm());
			//TODO fix: proper language tag handling
		} else if (o instanceof TypedLiteral) {
			object = factory.createLiteral(((TypedLiteral)o).getLexicalForm(), factory.createURI(((TypedLiteral)o).getDataType().toString()));
		}
		
		if (subject == null || object == null) {
			throw new RuntimeException("Subject or object is null. There is a bug.");
		}
		
		return factory.createStatement(subject, predicate, object);
	}
}
