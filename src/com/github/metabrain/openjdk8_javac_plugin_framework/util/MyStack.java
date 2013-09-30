package com.github.metabrain.openjdk8_javac_plugin_framework.util;

import java.util.*;

//FIXME enganei-me que estupidez, isto � suposto ser uma stack...
public class MyStack<T> implements Queue<T>{
	
	LinkedList<T> q ;

	public MyStack() {
		q = new LinkedList<T>() ;
	}

	private MyStack(LinkedList<T> l) {
		q = l ;
	}
 
	public MyStack<T> clone() {
		LinkedList<T> new_q = new LinkedList<T>() ;
		for(T t : q)
			new_q.addLast(t) ;
		return new MyStack<T>(new_q);		
	}
	
	public MyStack<T> reverse() {
		Collections.reverse(q) ;
		return this ;
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException() ;
		
	}

	@Override
	public boolean contains(Object arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public boolean isEmpty() {
		return q.isEmpty() ;
	}

	@Override
	public Iterator<T> iterator() {
		return q.iterator() ;
	}

	@Override
	public boolean remove(Object arg0) {
		return q.remove(arg0) ;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public int size() {
		return q.size() ;
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException() ;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public boolean add(T arg0) {
		q.addFirst(arg0) ;
		return true ;
	}

	@Override
	public T remove() {
		return q.removeFirst() ;
	}

	@Override
	public T element() {
		return q.getFirst() ;
	}

	@Override
	public boolean offer(T arg0) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public T peek() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public T poll() {
		throw new UnsupportedOperationException() ;
	}
}