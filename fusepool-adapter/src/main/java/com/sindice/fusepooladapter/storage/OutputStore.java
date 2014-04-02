/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepooladapter.storage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.jena.commons.Jena2TriaUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * storage for output graph
 * 
 * 
 */
public class OutputStore implements TripleCollection {
  private final String datafolder;
  private Dataset dataset;
  private final Jena2TriaUtil j2t;

  public OutputStore(String datafolder) {
    Path dataPath = Paths.get(datafolder);
    if (Files.exists(dataPath)){
      if (! Files.isDirectory(dataPath)){
        throw new IllegalArgumentException("file instead of folder specified for output data");
      }
    }
    this.datafolder = datafolder;
    this.j2t = new Jena2TriaUtil(new HashMap<Node, BNode>());

  }
  /**
   * cleans backing triplestore
   */
  public void clean() {
    try {
      Path dataPath = Paths.get(datafolder);
      if (Files.exists(dataPath)){
        DirectoryStream<Path> files = Files.newDirectoryStream(dataPath);
        if (files != null){
          for (Path filePath: files){
            Files.delete(filePath);
          }
        } 
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("wrong output data path", e);
    }
  }
  /**
   * prepares collection for reading intended to be used after backing triple store is populated
   */
  public void init() {
    dataset = TDBFactory.createDataset(datafolder);
    dataset.begin(ReadWrite.READ);
  }

  public void destroy() {
    dataset.end();
    dataset.close();
  }

  public boolean add(Triple e) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public boolean addAll(Collection<? extends Triple> c) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public void clear() {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public boolean contains(Object o) {
    if (!(o instanceof Triple)) {
      return false;
    }
    Triple triple = (Triple) o;
    return filter(triple.getSubject(), triple.getPredicate(), triple.getObject()).hasNext();
  }

  public boolean containsAll(Collection<?> c) {
    for (Object t : c) {
      if (!contains(t)) {
        return false;
      }
    }
    return true;
  }

  public boolean isEmpty() {
    return false;
  }

  public Iterator<Triple> iterator() {
    final StmtIterator sIterator = dataset.getDefaultModel().listStatements();
    return new ConvertingIterator(sIterator);
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public int size() {
    return (int) dataset.getDefaultModel().size();
  }

  public Object[] toArray() {
    throw new UnsupportedOperationException("it is too big set for memory object");
  }

  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException("it is too big set for memory object");
  }

  public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
    com.hp.hpl.jena.rdf.model.Resource jSubject = null;
    if (subject != null) {
      jSubject = dataset.getDefaultModel().createResource(
          subject.toString().substring(1, subject.toString().length() - 1));
    }
    com.hp.hpl.jena.rdf.model.Property jPredicate = null;
    if (predicate != null) {
      jPredicate = dataset.getDefaultModel().createProperty(
          predicate.toString().substring(1, predicate.toString().length() - 1));
    }
    StmtIterator sIterator = null;;
    if (object != null) {
      if (object.toString().charAt(0) == '<') {
        sIterator = dataset.getDefaultModel().listStatements(
            jSubject,
            jPredicate,
            dataset.getDefaultModel().createResource(
                object.toString().substring(1, object.toString().length() - 1)));
      } else {
        sIterator = dataset.getDefaultModel().listStatements(jSubject, jPredicate,
            object.toString());
      }
    } else {
      sIterator = dataset.getDefaultModel().listStatements(jSubject, jPredicate, (String) null);
    }

    return new ConvertingIterator(sIterator);
  }

  public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public void addGraphListener(GraphListener listener, FilterTriple filter) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  public void removeGraphListener(GraphListener listener) {
    throw new UnsupportedOperationException("it is immutable set");
  }

  /**
   * simple iterator that converts triples of fly
   * 
   * 
   */
  private class ConvertingIterator implements Iterator<Triple> {
    private StmtIterator sIterator;

    public ConvertingIterator(StmtIterator sIterator) {
      this.sIterator = sIterator;
    }

    public boolean hasNext() {
      return sIterator.hasNext();
    }

    public Triple next() {
      return j2t.convertTriple(sIterator.next().asTriple());
    }

    public void remove() {
      throw new UnsupportedOperationException("it is immutable set");
    }

  }
}
