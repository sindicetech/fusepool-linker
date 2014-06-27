package com.sindice.fusepooladapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.sindice.fusepooladapter.configuration.LinkerConfiguration;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.databases.LuceneDatabase;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 *    https://code.google.com/p/duke/wiki/DatabaseConfig
 *
 *    no.priv.garshol.duke.DebugCompare
 */
public class DebuggingLinkerAdapter extends LinkerAdapter {
    //private static final Logger logger = LoggerFactory.getLogger(LinkerAdapter.class);
    Path luceneDatabasePath;

    @Override
    public TripleCollection interlink(TripleCollection source, TripleCollection target, LinkerConfiguration configuration) {
        Configuration config = configuration.getDukeConfiguration();

        LuceneDatabase db = (LuceneDatabase) config.getDatabase(false);

        luceneDatabasePath = createTempDirectory("lucene-interlink-");
        db.setPath(luceneDatabasePath.toAbsolutePath().toString());

        Logger processor = (Logger) LoggerFactory.getLogger(Processor.class);
        processor.setLevel(Level.DEBUG);

        return super.interlink(source, target, configuration);
    }

    @Override
    public TripleCollection interlink(TripleCollection dataset, LinkerConfiguration configuration) {
        Configuration config = configuration.getDukeConfiguration();

        LuceneDatabase db = (LuceneDatabase) config.getDatabase(false);

        luceneDatabasePath = createTempDirectory("lucene-interlink-");
        db.setPath(luceneDatabasePath.toAbsolutePath().toString());

        Logger processor = (Logger) LoggerFactory.getLogger(Processor.class);
        processor.setLevel(Level.DEBUG);

        return super.interlink(dataset, configuration);
    }

    @Override
    public TripleCollection interlink(TripleCollection dataToInterlink) {
        return null;
    }

    @Override
    public TripleCollection interlink(TripleCollection dataset1, TripleCollection dataset2) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
