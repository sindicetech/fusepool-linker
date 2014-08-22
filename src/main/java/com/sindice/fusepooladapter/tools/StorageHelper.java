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
package com.sindice.fusepooladapter.tools;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.sindice.fusepooladapter.storage.JenaStoreTripleCollection;
import org.apache.clerezza.rdf.core.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class for managing Jena collections 
 *
 */
public class StorageHelper {
    private static Logger logger = LoggerFactory.getLogger(StorageHelper.class);

    
    private StorageHelper() {
        //hide utility class constructor
    }
    /**
     * Populates a Jena TDB store with the given dataset
     *
     */
    public static void loadTestCollectionFull() throws FileNotFoundException {
        loadTestCollectionFull("/data/tmp_fusepool/dbpedia-companies/dbpedia-companies-bigger.nt", "tmp/dbpediaBigJena", "N-TRIPLE");
    }

    public static void loadTestCollectionFull(String pathToInputFile, String pathToOutputDir, String format) throws FileNotFoundException {
        File collection = new File(pathToOutputDir);
        collection.delete();

        Dataset dataset = TDBFactory.createDataset(pathToOutputDir);
        dataset.begin(ReadWrite.WRITE);
        // Get model inside the transaction
        Model model = dataset.getDefaultModel();
        FileInputStream in = null;
        try {
            in = new FileInputStream(pathToInputFile);
            logger.info(pathToInputFile);
            model.read(in, null, format);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataset.commit();
        TDB.sync(dataset);
        dataset.end();
        logger.info("Loaded {} triples.", model.size());
    }

    public static int getJenaTripleCollectionSize(String jenaDirectory) {
        JenaStoreTripleCollection os = new JenaStoreTripleCollection(jenaDirectory);
        os.init();
        os.destroy();
        return os.size();
    }

    public static void printJenaTripleCollection(String jenaDirectory) {
        JenaStoreTripleCollection os = new JenaStoreTripleCollection(jenaDirectory);
        os.init();

        for (Triple triple : os) {
            System.out.println(triple);
        }
    }

    public static void printJenaTripleCollectionToFile(String jenaDirectory, String filePath) throws IOException {
        JenaStoreTripleCollection os = new JenaStoreTripleCollection(jenaDirectory);
        os.init();

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), Charset.forName("UTF-8"));
        for (Triple triple : os) {
            writer.write(triple.toString());
            writer.newLine();
        }
    }

    public static void main(String...args) throws Exception {
        loadTestCollectionFull();
    }
}
