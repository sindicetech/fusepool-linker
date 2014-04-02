/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * Fusepool-linker this is proprietary software do not use without authorization by Sindice Limited.
 */

package com.sindice.fusepool;

import no.priv.garshol.duke.Logger;

/**
 * Wrapper around {@link org.slf4j.Logger} that implements Duke's own {@link Logger} interface. 
 * 
 *
 */
public class DukeSlf4jLogger implements Logger {
	private org.slf4j.Logger logger;
	
	/**
	 * 
	 * @param logger The logger to which to forward method calls.
	 */
	public DukeSlf4jLogger(org.slf4j.Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void trace(String msg) {
		logger.trace(msg);
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public void debug(String msg) {
		logger.debug(msg);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void warn(String msg) {
		logger.warn(msg);
	}

	@Override
	public void warn(String msg, Throwable e) {
		logger.warn(msg, e);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void error(String msg) {
		logger.error(msg);;
	}

	@Override
	public void error(String msg, Throwable e) {
		logger.error(msg, e);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}
}
