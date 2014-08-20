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

import java.util.Stack;

/**
 * A simple stack-based stopwatch implementation for measuring execution time.
 */
public class StopWatch {
	private static Stack<Range> stack = new Stack<Range>();
	
	public static void start() {
		stack.push(new Range());
	}
	
	public static void end() {
		stack.peek().end = System.currentTimeMillis();
	}
	
	public static Long popTime() {
		if (stack.isEmpty()) {
			return null;
		}
		Range r = stack.pop();
		return r.end - r.start;
	}
		
	public static String popTimeString() {
		return String.format("%,d", popTime());
	}
	
	public static String popTimeString(String format) {
		return String.format(format, popTimeString());
	}
	
	public static void main(String args[]) throws InterruptedException {
		start();
		
		Thread.sleep(1500);
		
		end();
		
		System.out.println("time: "+popTimeString());
	}
}
