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
package com.sindice.fusepool.cleaners;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameCleanerTest {

	@Test
	public void test() {
		NameCleaner cleaner = new NameCleaner();
		assertEquals("Terrell, Thomas Gwyn",
				cleaner.clean("Terrell, Thomas Gwyn (GB)"));
		assertEquals(
				"Zumstein sen., Fritz",
				cleaner.clean("DELETED Zumstein sen., Fritz R. 102(2)a) 18.09.1985"));
		assertEquals(
				"Rioufrays, Roger",
				cleaner.clean("DELETED Rioufrays, Roger (FR) R. 102(1) 31.01.1996"));
		assertEquals(
				"Avellan-Hultman, Bengt Adolf",
				cleaner.clean("DELETED Avellan-Hultman, Bengt Adolf (SE) R. 102(2)a), deceased 02.05.1993"));
		assertEquals("Chevallier, René",
				cleaner.clean("DELETED Chevallier, René (deceased) 12.05.1980"));
		assertEquals(
				"Freiherr von Gravenreuth, Günter, Dipl.-Ing.",
				cleaner.clean("Freiherr von Gravenreuth, Günter, Dipl.-Ing. (FH)"));
		assertEquals("Pennant, Pyers", cleaner.clean("Pennant, Pyers"));
		assertEquals(
				"Grünecker, Kinkeldey, Stockmair &amp; Schwanhäusser Anwaltssozietät",
				cleaner.clean("Grünecker, Kinkeldey, Stockmair &amp; Schwanhäusser Anwaltssozietät"));
	}

}
