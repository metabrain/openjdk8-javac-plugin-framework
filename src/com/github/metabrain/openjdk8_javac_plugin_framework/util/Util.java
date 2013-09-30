package com.github.metabrain.openjdk8_javac_plugin_framework.util;


import java.util.LinkedList;
import java.util.Queue;

public class Util {

	public static <T> MyStack<T> clone(MyStack<T> q) {
		Queue<T> new_q = new LinkedList<T>() ;
		new_q.addAll(q) ;
		return q;		
	}
}
 