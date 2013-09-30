package com.github.metabrain.openjdk8_javac_plugin_framework.visitors;

import com.github.metabrain.openjdk8_javac_plugin_framework.api.AST_Query;
import com.github.metabrain.openjdk8_javac_plugin_framework.api.TreeTranslatorStateful;
import com.github.metabrain.openjdk8_javac_plugin_framework.util.ListUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeScanner;

import static com.sun.tools.javac.code.Flags.HASINIT;

/**
 * Created with IntelliJ IDEA.
 * User: MetaBrain
 * Date: 13-06-2013
 * Time: 10:31
 *
 * @autor metabrain (https://github.com/metabrain) - Daniel Parreira 2013
 */
public class IsolateReturnExpressionsVisitor {

	private static TreeTranslatorStateful state ;

	public static void isolate(JCMethodDecl method, TreeTranslatorStateful state) {
		IsolateReturnExpressionsVisitor.state = state;
		Isolator v = new Isolator();
		method.accept(v);
	}

	private static class Isolator extends TreeScanner {
		@Override
		public void visitReturn(JCReturn tree) {
//			System.out.println("return :"+tree.toString());
//			System.out.println("owner tree? :");
			if(tree.expr!=null && !(tree.expr instanceof JCIdent))    {
				DebugVisitor.debug(AST_Query.getEnclosingTree(tree, state));
				throw new RuntimeException("Shouldn't be explored because it should've been translated already from the upper node!");
			}

			super.visitReturn(tree);    //To change body of overridden methods use File | Settings | File Templates.
		}

		@Override
		public void visitBlock(JCBlock tree) {
			if(tree.stats!=null && tree.stats.size()>0) {
				//if this block has a return on the last statement and that return has a body
				if(tree.stats.last() instanceof JCReturn) {
					JCReturn ret = (JCReturn) tree.stats.last();
					if(ret.expr!=null){

						JCMethodDecl method = AST_Query.getEnclosingMethod(tree,state);//findType(ret.expr,state);
						Type type = AST_Query.getMethodReturnType(method,state);

//						System.out.println(retType);
//						System.out.println(retType.getClass());
						JCExpression init = ret.expr;//AST_Query.emptyConstructor("Partition", state);

//
						Symbol.VarSymbol var = new Symbol.VarSymbol(
								HASINIT,
								state.names.fromString("returnExpression"),
								type,
								method.sym) ;
						JCTree.JCVariableDecl new_var = state.make.VarDef(var, init) ;
//
//						System.out.println("RET with exp:"+tree.toString());

						tree.stats = ListUtil.append(tree.stats,new_var,tree.stats.length()-1);
						ret.expr=state.make.Ident(new_var);

//						System.out.println("RET with exp AFTER remove:"+tree.toString());
//
					}
					else {
//						System.out.println("RET with NO exp:"+tree.toString());
					}
				}
			}
			super.visitBlock(tree);    //To change body of overridden methods use File | Settings | File Templates.
		}
	}
}
