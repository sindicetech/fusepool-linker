package com.sindice.fusepooladapter.storage;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Header configuration holder for {@link org.supercsv.io.CsvMapWriter}.
 *
 * Note that #getProcessors() returns an array of {@link Optional}s of length #getHeader().length.
 */
public class CsvConfig {
    private String[] header;
    private CellProcessor[] processors;

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public CellProcessor[] getProcessors() {
        if (processors != null) {
            return processors;
        }

        if (header == null) {
            return null;
        }

        processors = new CellProcessor[header.length];

        for (int i = 0; i < header.length; i++) {
            processors[i] = new Optional();
        }

        return processors;
    }
}
