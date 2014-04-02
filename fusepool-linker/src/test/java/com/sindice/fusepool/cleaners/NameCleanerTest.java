/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
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
