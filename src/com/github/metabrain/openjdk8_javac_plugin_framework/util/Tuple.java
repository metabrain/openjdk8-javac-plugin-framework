package com.github.metabrain.openjdk8_javac_plugin_framework.util;

/**
 * Created with IntelliJ IDEA.
 * User: MetaBrain
 * Date: 23-05-2013
 * Time: 22:27
 * To change this template use File | Settings | File Templates.
 */
public class Tuple<T1
//		extends Comparable<T1>
		, T2>
//		implements Comparable<Tuple>
		{
	final T1 x;
	final T2 y;
	public T1 first() { return x ; }
	public T2 second() { return y ; }
	public Tuple(T1 xx, T2 yy) {
		this.x = xx ;
		this.y = yy ;
	}

//	@Override
//	public int compareTo(Tuple o) {
//		return o.first().compareTo(this.first());
//		//throw new RuntimeException("AUTOMATICALLY GENERATED METHOD: NOT IMPLEMENTED YET! IntelliJ IDEA");
//		//return 0;  //To change body of implemented methods use File | Settings | File Templates.
//	}
}
