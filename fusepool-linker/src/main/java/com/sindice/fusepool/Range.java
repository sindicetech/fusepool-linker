package com.sindice.fusepool;
class Range {
	public Range(long start) {
		this.start = start;
	}
	public Range() {
		this.start = System.currentTimeMillis();
	}

	public long start = 0;
	public long end = 0;
}