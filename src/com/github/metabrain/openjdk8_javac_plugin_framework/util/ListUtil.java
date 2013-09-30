package com.github.metabrain.openjdk8_javac_plugin_framework.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class ListUtil {
	
	/**
	 * Insert at start
	 * @param <T>
	 * @param l
	 * @return
	 */
	public static <T> List<T> prepend(List<T> list, T elem) {
		return append(list, elem, 0); 
	} 
	
	/**
	 * Append at end
	 * @param <T>
	 */
	public static <T> List<T> append(List<T> list, T elem) {
		return append(list, elem, list.size());
	}


	/**
	 * Inserts at position POS. Element previous at position POS goes to position POS+1
	 * @param list
	 * @param elem
	 * @param pos
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> append(List<T> list, T elem, int pos) {
		ListBuffer<T> buf = ListBuffer.lb();
		for(int i=0 ; i<list.size() ; i++) {
			if(i==pos) //insert
				buf.append(elem);
			buf.append(list.get(i));
		}

		if(pos==list.size()) //insert at end
			buf.append(elem);

		return buf.toList();
	}

	public static <T> List<T> remove(List<T> list, T to_replace) {
		ListBuffer lb = ListBuffer.lb();
		for(T cur : list) {
			if(cur!=to_replace)
				lb.add(cur);
			else {
//				System.out.println("YEP, EQUAL REMOVING! !");
			}
		}

		return lb.toList();  //To change body of created methods use File | Settings | File Templates.
	}


	public static <T> List<T> replace(List<T> list, T to_replace, T replacement) {
		ListBuffer lb = ListBuffer.lb();
		for(T cur : list) {
			if(cur!=to_replace)
				lb.add(cur);
			else {
				lb.add(replacement);
//				System.out.println("YEP, EQUAL REPLACING! !");
			}
		}

		return lb.toList();  //To change body of created methods use File | Settings | File Templates.
	}

	public static <T> List<T> merge(List<T>[] lvars) {
		ListBuffer<T> lb = ListBuffer.lb() ;
		for(List<T> l : lvars)
			for(T var : l)
				lb.append(var) ;

		return lb.toList();
	}

	public static List<JCTree> merge(java.util.List<JCTree.JCVariableDecl> a, java.util.List<JCTree.JCExpression> b, java.util.List<JCTree.JCExpression> c) {
		ListBuffer<JCTree> lb = ListBuffer.lb() ;
		for(JCTree.JCVariableDecl var : a)
			lb.append(var) ;
		for(JCTree.JCExpression var : b)
			lb.append(var) ;
		for(JCTree.JCExpression var : c)
			lb.append(var) ;

		return lb.toList();
	}
}
