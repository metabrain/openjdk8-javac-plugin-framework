package com.github.metabrain.openjdk8_javac_plugin_framework.visitors;

import com.github.metabrain.openjdk8_javac_plugin_framework.api.TreeTranslatorStateful;
import com.github.metabrain.openjdk8_javac_plugin_framework.api.AST_Query;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class FindFieldsVisitor extends TreeScanner {
	
		public final TreeTranslatorStateful state ;
		public final JCTree tree ;
		
		public FindFieldsVisitor(JCTree tree, TreeTranslatorStateful state) {
			this.tree = tree ;
			this.state = state ;
			this.lb = ListBuffer.lb() ;
		}

		public static List<JCExpression> find(JCTree tree, TreeTranslatorStateful state) {
//			DebugVisitor.debug(tree);
			
			FindFieldsVisitor visitor = new FindFieldsVisitor(tree, state);//, state);
			tree.accept(visitor);
			return visitor.lb.toList();
		}
		
		/*
		 * VISITOR METHODS
		 */
		public final ListBuffer<JCExpression> lb ;
		
		@Override
		public void visitIdent(JCIdent tree) {
			if(AST_Query.isField(tree,state))
				lb.add((JCExpression)tree) ;

			super.visitIdent(tree);
		}
		
		@Override
		public void visitSelect(JCFieldAccess tree) {
			
//			AST_Query.find
//			
//			Set<Modifier> mods = AST_Query.getModifiers(tree,state) ;
//			if(mods!=null){
//				System.out.println("NOT NULL:"+mods.toString());
//				for(Modifier m : mods)
//					System.out.println("mod:"+m.toString());
//			}

//			lb.add(tree);
			
			
//			System.out.println("field.type:"+tree.type);
//			System.out.println("field.sym:"+tree.sym);

//			AST_Query.findType(tree,state);
			
//			state.elementsC.getTypeElement(tree.selected.type.toString());
//	        ClassSymbol tsym = state.elementsC.getTypeElement(clazz) ;
//			state.treesC.

//			System.out.println("####");
//			System.out.println("field:          "+tree);
//			System.out.println("field.selected: "+tree.selected);

			if(AST_Query.isField(tree,state))
				lb.add((JCExpression)tree) ;

			super.visitSelect(tree); 
		}

}
