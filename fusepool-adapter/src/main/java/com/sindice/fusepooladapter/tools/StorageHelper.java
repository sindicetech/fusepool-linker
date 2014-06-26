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
 *
 */
public class StorageHelper {
    private static Logger logger = LoggerFactory.getLogger(StorageHelper.class);

    /**
     * Populates a Jena TDB store with the given dataset
     *
     */
    public static void loadTestCollectionFull() throws FileNotFoundException {
        loadTestCollectionFull("/data/tmp_fusepool/dbpedia-companies/dbpedia-companies-small.nt", "tmp/dbpediaSmallJena", "N-TRIPLE");
    }

    public static void loadTestCollectionFull(String pathToInputFile, String pathToDataset, String format) throws FileNotFoundException {
        File collection = new File(pathToDataset);
        collection.delete();

        Dataset dataset = TDBFactory.createDataset(pathToDataset);
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

    public static void main(String...args) {

    }
}
