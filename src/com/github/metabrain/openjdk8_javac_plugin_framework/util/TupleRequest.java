package com.github.metabrain.openjdk8_javac_plugin_framework.util;

/**
 * Created with IntelliJ IDEA.
 * User: MetaBrain
 * Date: 23-05-2013
 * Time: 22:27
 * To change this template use File | Settings | File Templates.
 */
public class TupleRequest<T1
//		extends Comparable<T1>
		, T2>
//		implements Comparable<Tuple>
		{
	final T1 x;
	final T2 y;
	public T1 first() { return x ; }
	public T2 second() { return y ; }
	public TupleRequest(T1 xx, T2 yy) {
		this.x = xx ;
		this.y = yy ;
	}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				TupleRequest that = (TupleRequest) o;

				if (!x.equals(that.x)) return false;
				if (!y.equals(that.y)) return false;

				return true;
			}

			@Override
			public int hashCode() {
				int result = x.hashCode();
				result = 31 * result + y.hashCode();
				return result;
			}


			//	@Override
//	public int compareTo(Tuple o) {
//		return o.first().compareTo(this.first());
//		//throw new RuntimeException("AUTOMATICALLY GENERATED METHOD: NOT IMPLEMENTED YET! IntelliJ IDEA");
//		//return 0;  //To change body of implemented methods use File | Settings | File Templates.
//	}
}
