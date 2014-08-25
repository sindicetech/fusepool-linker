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
		logger.error(msg);
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
