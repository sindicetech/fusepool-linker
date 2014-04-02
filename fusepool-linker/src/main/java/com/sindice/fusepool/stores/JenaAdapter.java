/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool.stores;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.sindice.fusepool.matchers.SimpleTriple;

public class JenaAdapter implements TripleWriter {
	private final static Logger logger = LoggerFactory
			.getLogger(JenaAdapter.class);
	private static final SimpleTriple SENTINEL = new SimpleTriple("SENTINEL", "SENTINEL", "SENTINEL");
	private final BlockingQueue<SimpleTriple> queue = new ArrayBlockingQueue<SimpleTriple>(
			10000);
    private final String datafolder;
    private volatile Thread thread;

	public JenaAdapter(String datafolder) {
		this.datafolder = datafolder;
	}
	
    @Override
	public boolean add(SimpleTriple triple) {
		try {
			queue.put(triple);
			return true;
		} catch (InterruptedException e) {
			logger.error("interupted during add", e);
		}
		return false;
	}

	@Override
	public void run() {
		this.thread = Thread.currentThread();
		Dataset dataset = TDBFactory.createDataset(datafolder);
		dataset.begin(ReadWrite.WRITE);
		// Get model inside the transaction
		Model model = dataset.getDefaultModel();
		model.removeAll();



		// new transaction for writing

		int cnt = 0;

			while (true) {
				SimpleTriple triple = null;
				try {
					triple = queue.take();
				} catch (InterruptedException e) {
					logger.error("interupted waiting for triple to process", e);
				}
				if (triple == SENTINEL){
					break;
				}
				model.add(model.createResource(triple.subject),
						model.createProperty(triple.predicate),
						model.createResource(triple.object));
				cnt++;
				if (cnt % 50000 == 0){
					logger.info(" {} triples were added ",cnt);
					
				}
			}
		
		dataset.commit();
		TDB.sync(dataset);
		dataset.end();
	}

	public void stop() {
		logger.info("adapter stopped");
		add(SENTINEL);
		//wait for me 
		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.error("interupted before storing thread finished", e);
		}
	}

	@Override
	public void close() throws IOException {
		stop();
		
	}

	@Override
	public void init() {
		this.thread = new Thread(this,"triplesWritter");
		this.thread.start();
	}

}
