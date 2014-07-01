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
package com.sindice.fusepooladapter.storage;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.datasources.Column;
import no.priv.garshol.duke.datasources.ColumnarDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extracts columns from Duke's {@link ColumnarDataSource} and returns it in the form of a {@link CsvConfig}.
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
