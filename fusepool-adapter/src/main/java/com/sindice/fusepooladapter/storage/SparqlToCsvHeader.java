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

import org.apache.commons.lang.NullArgumentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the header configuration for {@link org.supercsv.io.CsvMapWriter} from a given SPARQL query.
 */
public class SparqlToCsvHeader {
    /**
     * Extracts a configuration for {@link org.supercsv.io.CsvMapWriter} from a given SPARQL query.
     *
     * Assumes that the query has the form "SELECT ?var1, ?var2[,...] WHERE ..." (variables must be stated explicitly).
     *
     * @param query the query from which to extract the CSV
     * @return a {@link CsvConfig} for the given query.
     */
    public static CsvConfig transform(String query) {
        if (query == null) {
            throw new NullArgumentException("Query cannot be null");
        }
        CsvConfig config = new CsvConfig();
        Matcher matcher = Pattern.compile("SELECT(.*)WHERE", Pattern.CASE_INSENSITIVE).matcher(query);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Query must contain an explicit SELECT ... WHERE substring listing all the variables.");
        }
        String variables = matcher.group(1);
        if ("*".equals(variables.trim())) {
            throw new IllegalArgumentException("Query must contain an explicit SELECT ... WHERE substring listing all the variables.");
        }

        /*
        TODO: parse according to spec: http://www.w3.org/TR/sparql11-query/
[143]  	VAR1	  ::=  	'?' VARNAME
[144]  	VAR2	  ::=  	'$' VARNAME
[164]  	PN_CHARS_BASE	  ::=  	[A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
[165]  	PN_CHARS_U	  ::=  	PN_CHARS_BASE | '_'
[166]  	VARNAME	  ::=  	( PN_CHARS_U | [0-9] ) ( PN_CHARS_U | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040] )*
[167]  	PN_CHARS	  ::=  	PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
         */

        matcher = Pattern.compile("([?$]\\p{Alnum}+)").matcher(variables);
        List<String> vars = new ArrayList<>();
        while (matcher.find()) {
            vars.add(matcher.group().substring(1));
        }
        config.setHeader(vars.toArray(new String[0]));
        return config;
    }
}
