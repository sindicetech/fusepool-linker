/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sindice.fusepooladapter;

import eu.fusepool.datalifecycle.Interlinker;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Reto
 */
@Component(service = Object.class, immediate = true, property = {"javax.ws.rs=true"})
@Path("duke-linker")
public class Runner {
    
	
    private Interlinker linker;
    private Parser parser;
    
    @Activate
    public void activator() throws Exception {
    }
    
    @GET
    public String test() throws Exception {
        MGraph mGraph = new IndexedMGraph();
        parser.parse(mGraph,getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection interlinks = linker.interlink(mGraph, mGraph);
        return "found "+interlinks.size()+" links in patent-data-sample-short.ttl";
    }

    @Reference(target = "(component.name=com.sindice.fusepooladapter.LinkerAdapter)")
    public void setLinker(Interlinker linker) {
        this.linker = linker;
    }
    public void unsetLinker(Interlinker linker) {
        this.linker = null;
    }

    @Reference
    public void setParser(Parser parser) {
        this.parser = parser;
    }
    public void unsetParser(Parser parser) {
        this.parser = null;
    }
    
    
}

