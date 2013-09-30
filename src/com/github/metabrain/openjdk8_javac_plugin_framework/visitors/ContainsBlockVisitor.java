package com.github.metabrain.openjdk8_javac_plugin_framework.visitors;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * Created with IntelliJ IDEA.
 * User: MetaBrain
 * Date: 13-06-2013
 * Time: 10:31
 *
 * @autor metabrain (https://github.com/metabrain) - Daniel Parreira 2013
 */
public class ContainsBlockVisitor {

	public static boolean contains(JCBlock block, JCTree tree) {
		Contains v = new Contains(block);
		tree.accept(v);
		return v.result ;
	}

	private static class Contains extends TreeScanner {
		private final JCBlock target ;
		private boolean result = false ;

		Contains(JCBlock target) {
			this.target = target ;
		}

		@Override
		public void visitBlock(JCBlock tree) {
			if(tree == target)   {
				result = true ;
//				System.out.println("FOUND INNER BLOCK!!!!");
			}
			else
				super.visitBlock(tree);    //To change body of overridden methods use File | Settings | File Templates.
		}
	}
}
