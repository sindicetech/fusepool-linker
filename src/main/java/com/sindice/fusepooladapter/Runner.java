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

import eu.fusepool.datalifecycle.Interlinker;

/**
 * A Fusepool component that runs interlinking or deduplication upon a GET request to "/duke-linker".
 */
@Component(service = Object.class, immediate = true, property = {"javax.ws.rs=true"})
@Path("duke-linker")
public class Runner {
    
	
    private Interlinker deduplicator;
    private Interlinker linker;
    private Parser parser;
    
    @Activate
    public void activator() throws Exception {
    }
    
    @GET
    @Path("/dedup")
    public String dedup() throws Exception {
        System.out.println("Deduplicating");
        MGraph mGraph = new IndexedMGraph();
        parser.parse(mGraph,getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection interlinks = deduplicator.interlink(mGraph, mGraph);
        return "found "+interlinks.size()+" links in patent-data-sample-short.ttl";
    }

    @GET
    @Path("/link")
    public String link() throws Exception {
        System.out.println("Linking");
        MGraph mGraph = new IndexedMGraph();
        parser.parse(mGraph,getClass().getResourceAsStream("patent-data-sample-short.ttl"), SupportedFormat.TURTLE);
        TripleCollection interlinks = linker.interlink(mGraph, mGraph);
        return "found "+interlinks.size()+" links in patent-data-sample-short.ttl";
    }

    @Reference(target = "(component.name=com.sindice.com.sindice.fusepooladapter.PatentLinkerAdapter)")
    public void setDeduplicator(Interlinker deduplicator) {
        this.deduplicator = deduplicator;
    }
    public void unsetDeduplicator(Interlinker deduplicator) {
        this.deduplicator = null;
    }

    @Reference(target = "(component.name=com.sindice.com.sindice.fusepooladapter.PatentsDbpediaLinkerAdapter)")
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

