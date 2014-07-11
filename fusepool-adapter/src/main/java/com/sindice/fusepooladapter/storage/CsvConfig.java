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
