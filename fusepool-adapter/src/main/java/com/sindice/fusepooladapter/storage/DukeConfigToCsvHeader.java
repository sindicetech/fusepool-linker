package com.sindice.fusepooladapter.storage;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.datasources.Column;
import no.priv.garshol.duke.datasources.ColumnarDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class DukeConfigToCsvHeader {

    public static CsvConfig transform(ColumnarDataSource dataSource) {
        CsvConfig config = new CsvConfig();

        Collection<Column> columns = dataSource.getColumns();

        List<String> header = new ArrayList<>();
        for (Column column : columns) {
            header.add(column.getName());
        }

        config.setHeader(header.toArray(new String[]{}));

        return config;
    }
}
