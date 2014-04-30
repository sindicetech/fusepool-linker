package com.sindice.fusepool;

import java.util.Stack;

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
