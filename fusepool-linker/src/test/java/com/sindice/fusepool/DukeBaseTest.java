/*
 * Created by Sindice LTD http://sindicetech.com
 * Sindice LTD licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sindice.fusepool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import no.priv.garshol.duke.CompactRecord;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.RecordImpl;
import no.priv.garshol.duke.RecordIterator;
import no.priv.garshol.duke.datasources.InMemoryDataSource;
import no.priv.garshol.duke.matchers.AbstractMatchListener;
import no.priv.garshol.duke.utils.DefaultRecordIterator;

public class DukeBaseTest {

	public static Record makeRecord() {
		return new RecordImpl(new HashMap());
	}

	public static Record makeRecord(String p1, String v1) {
		return makeRecord(p1, v1, null, null, null, null);
	}

	public static Record makeRecord(String p1, String v1, String p2, String v2) {
		return makeRecord(p1, v1, p2, v2, null, null);
	}

	public static Record makeRecord(String p1, String v1, String p2, String v2,
			String p3, String v3) {
		CompactRecord rec = new CompactRecord();
		rec.addValue(p1, v1);
		if (v2 != null)
			rec.addValue(p2, v2);
		if (v3 != null)
			rec.addValue(p3, v3);
		return rec;
	}

	public static class TestListener extends AbstractMatchListener {
		private List<Pair> matches;
		private int records;
		private int nomatch;
		private int maybes;

		public TestListener() {
			this.matches = new ArrayList();
		}

		public List<Pair> getMatches() {
			return matches;
		}

		public int getRecordCount() {
			return records;
		}

		public int getNoMatchCount() {
			return nomatch;
		}

		public int getMaybeCount() {
			return maybes;
		}

		public void batchReady(int size) {
			records += size;
		}

		public void matches(Record r1, Record r2, double confidence) {
			matches.add(new Pair(r1, r2, confidence));
		}

		public void matchesPerhaps(Record r1, Record r2, double confidence) {
			maybes++;
		}

		public void noMatchFor(Record r) {
			nomatch++;
		}
	}

	public static class Pair {
		public Record r1;
		public Record r2;
		public double conf;

		public Pair(Record r1, Record r2, double conf) {
			this.r1 = r1;
			this.r2 = r2;
			this.conf = conf;
		}
	}

	public static class TestDataSource extends InMemoryDataSource {
		private int batch_count;

		public TestDataSource(Collection<Record> records) {
			super(records);
		}

		public RecordIterator getRecords() {
			return new TestRecordIterator(this, records.iterator());
		}

		public void batchProcessed() {
			batch_count++;
		}

		public int getBatchCount() {
			return batch_count;
		}
	}

	static class TestRecordIterator extends DefaultRecordIterator {
		private TestDataSource source;

		public TestRecordIterator(TestDataSource source, Iterator<Record> it) {
			super(it);
			this.source = source;
		}

		public void batchProcessed() {
			source.batchProcessed();
		}
	}
}