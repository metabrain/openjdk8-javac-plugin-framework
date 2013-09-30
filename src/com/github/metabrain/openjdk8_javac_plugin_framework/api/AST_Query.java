package com.github.metabrain.openjdk8_javac_plugin_framework.api;

import com.github.metabrain.openjdk8_javac_plugin_framework.util.ListUtil;
import com.github.metabrain.openjdk8_javac_plugin_framework.util.Tuple;
import com.github.metabrain.openjdk8_javac_plugin_framework.visitors.DebugVisitor;
import com.github.metabrain.openjdk8_javac_plugin_framework.visitors.FindFieldsVisitor;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

import static com.sun.tools.javac.tree.JCTree.*;

public class AST_Query {

	
	 
	/**
	 * Returns if an AST node has the matching annotation
	 */
	public static boolean hasAnnotation(JCVariableDecl tree,
			String annotationName) {
		for (JCAnnotation note : tree.getModifiers().getAnnotations()) {
			// System.out.println("############");
			// System.out.println("size annotations: "+tree.getModifiers().getAnnotations().size());
			// System.out.println("variable: "+tree);
			// System.out.println("var.init: "+tree.init);
			// System.out.println("var.mods: "+tree.mods);
			// System.out.println("var.mods.type: "+tree.mods.type);
			// System.out.println("var.mods.annotations: "+tree.mods.annotations);
			// System.out.println("var.mods.annotations.head: "+tree.mods.annotations.head);
			// System.out.println("var.mods.annotations.head.annotationType: "+tree.mods.annotations.head.annotationType);
			// System.out.println("var.mods.annotations.head.annotationType.type: "+tree.mods.annotations.head.annotationType.type);
			// System.out.println("var.name: "+tree.name);
			// System.out.println("var.sym: "+tree.sym);
			// System.out.println("var.type: "+tree.type);
			// System.out.println("var.vartype: "+tree.vartype);
			// System.out.println("annotationType: "+note.annotationType);
			// System.out.println("annotationType.toString(): "+note.annotationType.toString());
			// System.out.println("attribute: "+note.attribute);
			// System.out.println("pos: "+note.pos);
			// System.out.println("args: "+note.args);
			// System.out.println("args.size: "+note.args.size());
			// System.out.println("jcident.type: "+((JCIdent)(note.getAnnotationType())).type);
			// System.out.println("jcident.sym: "+((JCIdent)(note.getAnnotationType())).sym);
			// System.out.println("getAnnotationType.type: "+note.getAnnotationType().type);
			// System.out.println("type:"+note.annotationType.type);

			// For parameters in functions (type not null)
			if (note.annotationType.type != null
					&& note.annotationType.type.toString().equals(
							annotationName)) {
				// System.out.println("mod:"+annotation.annotationType);
				return true;
			}

			// For variable declarations on method bodies the type is null, so I
			// have to use
			// toString() to know the annotation type...
//			System.out.println("note.annotationType.toString(): "
//					+ note.annotationType.toString());
//			System.out.println("annotationName: " + annotationName);
			
			// FIXME mudar o atomic para algo mais flexivel
			if (note.annotationType.toString().equals("Atomic")) {
				return true;
			}
		}

		return false; // None was found
	}




	public static JCExpression typeFromString(String type, TreeTranslatorStateful instance) {
		return instance.make.Ident(instance.names.fromString(type));
	}
 
	protected static void removeAnnotation(JCVariableDecl var,
			TreeTranslatorStateful instance, String toRemove) {
//		System.out.println("\n\n#############\nBEFORE\n\n");
//		System.out.println(var);

		ListBuffer<JCAnnotation> lb = ListBuffer.lb();

		for (JCAnnotation note : var.mods.annotations) {
			if (!note.annotationType.toString().equals(toRemove)) {
				lb.add(note);
			}
		}

		var.mods.annotations = lb.toList();
		
//		System.out.println(var);
//		System.out.println("\n\nAFTER\n#############\n");
	}

	public static JCExpression changeConstructorType(JCExpression init, String newClazzName, TreeTranslatorStateful instance) {
		if(init instanceof JCNewClass) { //constructor
			JCNewClass c = (JCNewClass) (init) ;

			JCNewClass newConstructor = instance.make.NewClass(
					null,//c.getEnclosingExpression(),
					null,//c.getTypeArguments(),
					instance.make.Ident(instance.names.fromString(newClazzName)),//clazzIdent,//c.getIdentifier(),//instance.make.ClassLiteral(newClazz),
					c.getArguments(),
					null);//c.getClassBody());//decl);

			return (JCExpression)newConstructor ;
		}
		//CHeck initializer if it's a new atomic array class eg.:
		// 		'... = new XString[...];'
		else if(init instanceof JCNewArray) { //constructor
			JCNewArray array = (JCNewArray) (init) ;

			//Remove "[]" from class name if it's an array
			JCIdent clazzIdent = instance.make.Ident(instance.names.fromString(newClazzName.replace("[]", "")));

			JCNewArray newConstructor = instance.make.NewArray(
					clazzIdent,
					array.dims,
					array.elems
			);

			return (JCExpression)newConstructor ;
		}
//		else if(init instanceof JCIdent) { //constructor
//			//var assingment, do nothing
//			return init ;
//		}
//
//		DebugVisitor.debug(init);
//		throw new RuntimeException("Constructor doesnt match JCNewClass or JCNewArray!");
		return init;
	}

	protected static void changeConstructor(JCVariableDecl var,
			TreeTranslatorStateful instance, ClassSymbol newClazz, String newClazzName) {
		//Check initializer if it's a new class eg.:
		// 		'... = new XString("...");'
		if(var.init!=null && !(var.init instanceof JCLiteral)) {
			var.init = changeConstructorType(var.init,newClazzName,instance);
		}
	} 

	
	public static JCNewClass constructor(String clazz, List<JCExpression> params, TreeTranslatorStateful instance) {
		return instance.make.NewClass(
				null,//c.getEnclosingExpression(), 
				null,//c.getTypeArguments(),  
				instance.make.Ident(instance.names.fromString(clazz)),//clazzIdent,//c.getIdentifier(),//instance.make.ClassLiteral(newClazz),
				params, 
				null);//c.getClassBody());//decl); 
		
	}

	/**
	 * Should cast to (JCExpression) to use in JCVariableDecl's "init" field
	 * @param clazz
	 * @param instance
	 * @return
	 */
	public static JCNewClass emptyConstructor(String clazz, TreeTranslatorStateful instance) {		
		return constructor(clazz, List.<JCExpression>nil(), instance) ; 
	}


	public static JCExpressionStatement invoke(String varName, String methodName, String returnTypeName, TreeTranslatorStateful instance) {
		JCExpression ident = (JCExpression)instance.make.Ident(instance.names.fromString(varName)) ;
		JCFieldAccess field = instance.make.Select(ident, instance.names.fromString(methodName));
		field.type = instance.manipulator.stringToType(returnTypeName) ;
		JCMethodInvocation invocation = instance.make.App(field) ;
		JCExpressionStatement exec = instance.make.Exec(invocation) ;

		return exec ;
	}

	public static JCExpressionStatement invoke(JCExpression varField, String methodName, String returnTypeName, TreeTranslatorStateful instance) {
		JCFieldAccess field = instance.make.Select(varField, instance.names.fromString(methodName));
		field.type = instance.manipulator.stringToType(returnTypeName) ;
		JCMethodInvocation invocation = instance.make.App(field) ;
		JCExpressionStatement exec = instance.make.Exec(invocation) ;

		return exec ;
	}

	public static JCExpressionStatement invoke(String varName, String methodName, String returnTypeName, 
			List<JCExpression> args, TreeTranslatorStateful instance) {

		JCExpression ident = (JCExpression)instance.make.Ident(instance.names.fromString(varName)) ;
		JCFieldAccess field = instance.make.Select(ident, instance.names.fromString(methodName));
		field.type = instance.manipulator.stringToType(returnTypeName) ;
		JCMethodInvocation invocation = instance.make.App(field,args) ;
		JCExpressionStatement exec = instance.make.Exec(invocation) ;
		
		return exec ; 
	}

	
	public static List<JCExpression> getFields(JCStatement s, TreeTranslatorStateful state) {
		return FindFieldsVisitor.find(s, state) ;
	}

	
	/**
	 * Returns the type of this identifier in the visible scope of a JCTree.
	 */
	public static String findTypeInScope(JCTree t, String ident) {
		throw new RuntimeException("not implememnted") ;
//		return null ;
	}

	public static Entry<TypeMirror,String> findTypeInvokedOn(JCMethodInvocation invoke, TreeTranslatorStateful state/*, JCClassDecl clazz*/) {
//		System.out.println("invoke:"+invoke.toString());
//		System.out.println("invoke.meth:"+invoke.meth.toString());
//		System.out.println("invoke.meth class:"+invoke.meth.getClass().toString());
//		System.out.println("invoke.meth.selected:"+((JCFieldAccess)(invoke.meth)).selected.toString());
//		System.out.println("invoke.meth.selected class:"+((JCFieldAccess)(invoke.meth)).selected.getClass().toString());
		
		JCTree objAccesed = null ;
		final TypeMirror clazzType ;

		//If invoked on function wihtout other trailing fields eg.: fun() (function in same class)
		if(invoke.meth instanceof JCIdent)
			objAccesed = getEnclosingClass(invoke,state) ; 
		else
			objAccesed = ((JCFieldAccess)(invoke.meth)).selected;
		clazzType = AST_Query.findType(objAccesed, state) ;
		//Funciona se for mais de um ponto (eg.: System.out.println())
//		if((((JCFieldAccess)(invoke.meth)).selected) instanceof JCFieldAccess) {
//			objAccesed = (JCFieldAccess) ((JCFieldAccess)(invoke.meth)).selected;
//			clazzType = AST_Query.findType(objAccesed, state) ;
//		}
//		//When it's directly called on static classes eg.: System.currentTimeMillis()
//		else if((((JCFieldAccess)(invoke.meth)).selected) instanceof JCIdent) {
//			clazzType = findType((JCIdent)(((JCFieldAccess)(invoke.meth)).selected), state);
//		}
//		else if() {
//			//is method invocation, so next field access is the method signature itself, skip to that
//			objAccesed = (JCFieldAccess) ((JCFieldAccess)(invoke.meth)).selected;
//			clazzType = AST_Query.findType(objAccesed, state) ;
//		}
//		else {
//			throw new RuntimeException("Not implemented!"+((((JCFieldAccess)(invoke.meth)).selected)).getClass());
//		}

//		if(objAccesed!=null) //se for field.field. ... .field.print();
//			clazzType = AST_Query.findType(objAccesed, state) ;
//		else //se for "print()";
//			clazzType = (AST_Query.convert2Element(clazz, state)).asType() ;
		
		
		String str = (invoke.meth.toString()) ;
		String[] splits = str.split("\\.") ;
		final String methodName = splits[splits.length-1]; //AST_Query.findType((JCFieldAccess) invoke.meth, state).toString() ;
		 
//		System.out.printf("Entry<clazztype:%s, methodName:%s>\n", clazzType, methodName);
		
		return new Entry<TypeMirror, String>() {
			@Override
			public String setValue(String arg0) {
				throw new RuntimeException("NOT IMPLEMTENDED LOL");
			}
			
			@Override
			public TypeMirror getKey() {
				// TODO Auto-generated method stub
				return clazzType;
			}

			@Override
			public String getValue() {
				return methodName;
			}
		} ;
	}

	public static JCClassDecl getEnclosingClass(JCTree tree, TreeTranslatorStateful state) {
		Iterator<Tree> p = state.treesC.getPath(state.jcc, tree).iterator() ;
		while(p.hasNext()) {
			Tree t = p.next();
//			System.out.println("is element null?"+(state.treesC.getElement(state.trees.getPath(state.jcc, t))==null));
//			System.out.println("clazztype:"+t.getClass()+"	\t classpath:"+t.toString());

			if(t instanceof JCClassDecl)
				return (JCClassDecl)t ;
		}
		throw new RuntimeException("didnt find enclosing class...");
	}
	public static JCMethodDecl getEnclosingMethod(JCTree tree, TreeTranslatorStateful state) {
		Iterator<Tree> p = state.treesC.getPath(state.jcc, tree).iterator() ;
		while(p.hasNext()) {
			Tree t = p.next();
//			System.out.println("is element null?"+(state.treesC.getElement(state.trees.getPath(state.jcc, t))==null));
//			System.out.println("clazztype:"+t.getClass()+"	\t classpath:"+t.toString());

			if(t instanceof JCMethodDecl)
				return (JCMethodDecl)t ;
		}
		throw new RuntimeException("didnt find enclosing class...");
	}

	public static JCTree.JCCompilationUnit getEnclosingCompilationUnit(JCTree tree, ListBuffer<Tuple<JCCompilationUnit, JavacTask>> compilation_units, TreeTranslatorStateful state) {
		for(Tuple<JCTree.JCCompilationUnit, JavacTask> tuple : compilation_units) {
			JCTree.JCCompilationUnit jcc = tuple.first() ;
			TreePath path = state.treesC.getPath(jcc, tree) ;
			if(path==null)
					continue;

			Iterator<Tree> p = path.iterator() ;
			while(p.hasNext()) {
				Tree t = p.next();
//			System.out.println("is element null?"+(state.treesC.getElement(state.trees.getPath(state.jcc, t))==null));
//			System.out.println("clazztype:"+t.getClass()+"	\t classpath:"+t.toString());

				if(t instanceof JCCompilationUnit)
					return (JCCompilationUnit)t ;
			}
		}

		JCCompilationUnit jcc = state.jcc ;
		TreePath path = state.treesC.getPath(jcc, tree) ;
		Iterator<Tree> p = path.iterator() ;
		while(p.hasNext()) {
			Tree t = p.next();
//			System.out.println("is element null?"+(state.treesC.getElement(state.trees.getPath(state.jcc, t))==null));
//			System.out.println("clazztype:"+t.getClass()+"	\t classpath:"+t.toString());

			if(t instanceof JCCompilationUnit)
				return (JCCompilationUnit)t ;
		}
		throw new RuntimeException("didnt find enclosing class...");
	}

	public static JCTree getEnclosingTree(JCTree tree, TreeTranslatorStateful state) {
		Iterator<Tree> p = state.treesC.getPath(state.jcc, tree).iterator() ;
		p.next();//current tree (me)
		return (JCTree)p.next();//enconsling node
	}

	public static JCClassDecl getEnclosingClass(JCTree tree, TreeScannerStateful state) {
		Iterator<Tree> p = state.treesC.getPath(state.jcc, tree).iterator() ;
		while(p.hasNext()) {
			Tree t = p.next();
//			System.out.println("is element null?"+(state.treesC.getElement(state.trees.getPath(state.jcc, t))==null));
//			System.out.println("clazztype:"+t.getClass()+"	\t classpath:"+t.toString());

			if(t instanceof JCClassDecl)
				return (JCClassDecl)t ;
		}
		throw new RuntimeException("didnt find enclosing class...");
	}

	protected static TypeMirror findType(JCIdent ident, TreeTranslatorStateful state) {
     	Element idElem = inScope(ident, state);
     	TypeMirror type = idElem.asType() ;
//		System.out.println("getCLAS:"+type.getClass());

     	return removeAnnotatedType(type) ;
	}

	public static TypeMirror removeAnnotatedType(TypeMirror type) {
		if(type instanceof Type.AnnotatedType) {
			type = ((Type.AnnotatedType) type).underlyingType;
//			System.out.println("TRUE class type:"+type.toString());
		}
		return type ;
	}
 
//	public static TypeMirror findType(JCMethodInvocation inv, TreeTranslatorStateful state) {
//		System.out.println("invocation:"+inv);
//     	JavacScope scp = state.treesC.getScope(state.trees.getPath(state.jcc, inv)) ;
//     	//TODO DO T HIS !!!
//     	
////     	if(inv.meth)�
//     	System.out.println("mneth:"+((JCFieldAccess)inv.meth).selected.toString());
//     	
//     	if(((JCFieldAccess)inv.meth).selected!=null) 
//     		return findType((JCFieldAccess)(((JCFieldAccess)inv.meth).selected), state);
//     	else
//     		throw new RuntimeException("NOT IMPLEMENTED LOL");
//	}

	protected static TypeMirror findType(JCVariableDecl var, TreeTranslatorStateful state) {
//		System.out.println("var.type:"+var.type);
//		System.out.println("var.vartype:"+var.vartype);
		

     	Element varElem = convert2Element(var, state);
     	TypeMirror type = varElem.asType() ;

//		System.out.println("varElem.asType():"+type);
		
		return removeAnnotatedType(type) ;
	}
	
	protected static TypeMirror findType(JCClassDecl clazz, TreeTranslatorStateful state) {
//		System.out.println("CLAZZtype:"+convert2Element(clazz, state).asType());
		return convert2Element(clazz, state).asType();
	}
	
	public static TypeMirror findType(JCTree tree, TreeTranslatorStateful state) {
		if(tree instanceof JCIdent) {
			return findType((JCIdent)tree, state) ;			
		}
		else if(tree instanceof JCMethodInvocation) {
			//peel the invocation
			return findType(((JCMethodInvocation)tree).meth,state) ;
		}
		else if(tree instanceof JCArrayAccess) {
			//peel the arrays
			JCTree t = (JCArrayAccess)tree ;
			while(t instanceof JCArrayAccess) {
				t = ((JCArrayAccess)t).getExpression() ;
			}
			return findType(t,state) ;
		}
		else if(tree instanceof JCNewArray) {
			//peel the arrays
			JCTree t = (JCNewArray)tree ;
			while(t instanceof JCNewArray) {
				t = ((JCNewArray)t).getType() ;
			}
			return findType(t,state) ;
		}
		else if(tree instanceof JCClassDecl) {
			return findType((JCClassDecl)tree, state) ;
		}
		else if(tree instanceof JCBinary) {
			//FIXME nao deve ser só isto... se for float+double nao dá o da esquerda
			JCBinary bin = (JCBinary)tree;
//			System.out.println("RESULTTTTTT:"+findType(bin.rhs, state));
			throw new RuntimeException("JCBinary type evaluation incorrect/incomplete...");
//			return findType(bin.lhs, state) ;
		}
		else if(tree instanceof JCFieldAccess) {
			return findType((JCFieldAccess)tree, state) ;			
		}
		else if(tree instanceof JCVariableDecl) {
			return findType((JCVariableDecl)tree, state) ;
		}
		else if(tree instanceof JCParens) {
			return findType(((JCParens)tree).expr, state) ;
		}
		else if(tree instanceof JCTypeCast) {
			return findType(((JCTypeCast)tree).clazz, state) ;
		}
		else if(tree instanceof JCNewClass) {
			return findType(((JCNewClass)tree).clazz, state) ;
		}
		else if(tree instanceof JCAssign) {
			return findType(((JCAssign)tree).lhs, state) ;
		}
		else if(tree instanceof JCLiteral) {
			JCLiteral literal = (JCLiteral)tree ;
			if(literal.getKind()==Kind.STRING_LITERAL)
				return state.elements.getTypeElement("java.lang.String").asType() ;

			throw new RuntimeException("findType for literal kind:"+literal.getKind()+" is not implemented!");
//			System.out.println("type:"+((TypeMirror)(((JCLiteral)tree).type)));
//			DebugVisitor.debug((((JCLiteral)tree)));
//			return (((JCLiteral)tree).type) ;
		}
		else if(tree instanceof JCPrimitiveTypeTree) {
			JCPrimitiveTypeTree primitive = (JCPrimitiveTypeTree)tree ;
//			System.out.println(primitive.getKind());
//			System.out.println(primitive.getPrimitiveTypeKind());
			if(primitive.getPrimitiveTypeKind()== TypeKind.INT)
				return state.elements.getTypeElement("java.lang.Integer").asType() ;
			if(primitive.getPrimitiveTypeKind()== TypeKind.BOOLEAN)
				return state.elements.getTypeElement("java.lang.Boolean").asType() ;
			if(primitive.getPrimitiveTypeKind()== TypeKind.DOUBLE)
				return state.elements.getTypeElement("java.lang.Double").asType() ;
			if(primitive.getPrimitiveTypeKind()== TypeKind.FLOAT)
				return state.elements.getTypeElement("java.lang.Float").asType() ;
			if(primitive.getPrimitiveTypeKind()== TypeKind.BYTE)
				return state.elements.getTypeElement("java.lang.Byte").asType() ;
			if(primitive.getPrimitiveTypeKind()== TypeKind.LONG)
				return state.elements.getTypeElement("java.lang.Long").asType() ;
			DebugVisitor.debug(tree);
			throw new RuntimeException("("+primitive.toString()+") findType for primitive kind:"+primitive.getKind()+" is not implemented!");
//			System.out.println("type:"+((TypeMirror)(((JCLiteral)tree).type)));
//			DebugVisitor.debug((((JCLiteral)tree)));
//			return (((JCLiteral)tree).type) ;
		}
		else if(tree instanceof JCArrayTypeTree) {
			JCArrayTypeTree arrayTree = ((JCArrayTypeTree)tree) ;
			if(arrayTree.elemtype.toString().equals("Object"))
				return state.elements.getTypeElement("java.lang.Object").asType() ;
			else
				return state.elements.getTypeElement(arrayTree.elemtype.toString()).asType() ;
//			System.out.println(arrayTree.getType());
//			System.out.println(arrayTree.elemtype);
//			System.out.println(arrayTree.getTag());
//			System.out.println(arrayTree.getKind());
//			System.out.println();
//			System.out.println(tree);
//			DebugVisitor.debug(tree);
//			throw new RuntimeException("findType for class:"+tree.getClass()+" is not implemented!");
		}
		System.out.println();
		System.out.println(tree);
		DebugVisitor.debug(tree);
		throw new RuntimeException("findType for class:"+tree.getClass()+" is not implemented!");
	}		
	
	protected static TypeMirror findType(JCFieldAccess tree, TreeTranslatorStateful state) {
//		System.out.printf("finding type of tree %s\n",tree.toString());

     	JavacScope scp = state.treesC.getScope(state.trees.getPath(state.jcc, tree)) ;
     	
     	Stack<JCFieldAccess> fields = new Stack<JCFieldAccess>() ;

		TypeMirror type ;

		//DAT HACKKK
		if(tree.toString().contains("java.lang.")) {
//			System.out.println("TESTING TYPE FOR "+tree.toString());
			String rootLangClass = "java.lang."+tree.toString().split("\\.")[2];
//			System.out.println("RTOOT LANG CLASS:"+rootLangClass);
			type = state.elementsC.getTypeElement(rootLangClass).type ;
//			return (TypeMirror) state.elementsC.getTypeElement(tree.toString()).type;
		}
		else {
	//     	fields.push(tree);

			//find outter most identifier
			JCTree selected = tree ;
			while(!(selected instanceof JCIdent)) {
				JCFieldAccess next = null ;
				if(selected instanceof JCFieldAccess) {
					next = ((JCFieldAccess)selected);
				}
				else if(selected instanceof JCParens) { //FIXME FIX FIX TA TUDO AH PEDREIRO
					selected = ((JCParens)selected).getExpression();
					continue;
				}
				else if(selected instanceof JCAssign) { //FIXME FIX FIX TA TUDO AH PEDREIRO
					selected = ((JCAssign)selected).lhs;
					continue;
				}
				else if(selected instanceof JCMethodInvocation)
					next = ((JCFieldAccess)(((JCMethodInvocation)selected).meth));
				else if(selected instanceof JCNewClass)   {
					selected = ((((JCNewClass)selected).clazz));
					break ; //found JCIdent
				}
				else if(selected instanceof JCArrayAccess) {
					selected = (((JCArrayAccess)selected).indexed);
					break ; //found JCIdent
				}
				else if(selected instanceof JCTypeCast) {
					selected = (((JCTypeCast)selected).clazz);
					break ; //found JCIdent
				}
				else                                 {
					DebugVisitor.debug(selected);
					System.out.println(selected.getClass());
					throw new RuntimeException("ups");
				}


				fields.push(next);
				selected = (next).selected ;
			}

			JCIdent rootIdent = (JCIdent)selected ;

			if(!isInScope(rootIdent, state)) {
				//devia encontrar sempre...
				System.out.println("################\nPrinting fields:");
				for(int i = 0 ; i<fields.size() ; i++)
					System.out.printf("\t[%d] -> %s\n",i,fields.get(i));
				System.out.println("################\n");
				System.out.println(rootIdent);
				System.out.println();
				DebugVisitor.debug(rootIdent);
				throw new RuntimeException("NAO TA' NO SCOPE!!!");
			}

			//encontrei
			type = findType(rootIdent, state) ;
		}


     	 
     	//localizar o seu tipo ...e o field que vem a seguir da classe localizada
     	//	enquanto nao for o ultimo field
     	JCFieldAccess nextField ;
     	Element found = null ;
//		System.out.println("remaining pops:"+fields.size());
		while(!fields.isEmpty()) {
			while(type instanceof Type.MethodType) {
				type = ((Type.MethodType) type).getReturnType();
			}
//			System.out.println("type:"+type);
			String clazzName = removeArray(removePolymorficParams(removeAnnotatedType(type).toString())).replaceAll("\\(.*\\)", "") ;
			clazzName = clazzName.replaceAll("\\(.*\\)", "");
//			System.out.println("clazzname replaces:"+clazzName);
//			System.out.println("trying to get class from "+clazzName+ " ("+type.toString()+" | "+type.getClass()+")");

			//FIXME hack feio... ver que se passa no RBTree
			if(state.elements.getTypeElement(clazzName)==null && clazzName.contains("AtomicGEN")) { //FIXME TIRAR ISTO DE HARDCODED
				clazzName = clazzName.replace("AtomicGEN","");//FIXME TIRAR ISTO DE HARDCODED
				Element datElement = inScope(clazzName,tree,state);
//				System.out.println("ELEMENT:");
//				System.out.println("ELEMENT:"+datElement);
				type = datElement.asType();
				clazzName = removeArray(removePolymorficParams(removeAnnotatedType(type).toString())).replaceAll("\\(.*\\)", "") ;
				clazzName = clazzName.replaceAll("\\(.*\\)", "");
//
////				System.out.println("ELEMENT:"+datElement.getClass());
//				System.out.println("tree:"+tree);
//				List<Element> list = variablesInScope(tree, state) ;
//				for(Element kkk : list)
//					System.out.println(">>>>>>>>>>"+kkk);
			}

			//Unfold native types if needed
			if(clazzName.equals("byte"))
				clazzName = "java.lang.Byte" ;
			else if(clazzName.equals("long"))
				clazzName = "java.lang.Long" ;
			else if(clazzName.equals("int"))
				clazzName = "java.lang.Integer" ;
			else if(clazzName.equals("boolean"))
				clazzName = "java.lang.Boolean" ;

			TypeElement clazz = state.elements.getTypeElement(clazzName) ;
//			TypeMirror clazz = state.elements.getTypeElement(clazzName).asType() ;

    		nextField = fields.pop() ;
//			System.out.printf("\t type=%s\n", type.toString());
//			System.out.printf("\t nextField=%s\n", nextField);
//			System.out.println("clazz:"+clazz);

			if(clazz==null) {
				System.out.println("cur field :"+nextField);
				while(!fields.isEmpty()) {
					System.out.println("still to pop:"+fields.pop());
				}
				DebugVisitor.debug(tree);
				throw new NullPointerException("Clazz is null!!");
			}
			else {
				out:while(true) {
					for(Element e : clazz.getEnclosedElements()) {
//						System.out.printf("FOR %s == %s?\n",e.getSimpleName().toString(),nextField.getIdentifier().toString());
//						System.out.println("checksout? "+e.getSimpleName().toString().equals(nextField.getIdentifier().toString()));
						if(e.getSimpleName().toString().equals(nextField.getIdentifier().toString())) {
							type = removeAnnotatedType(e.asType()) ;
							found = e ;
//							System.out.printf("%s -> %s as type:%s\n",e.getSimpleName().toString(),nextField.getIdentifier(),type);
//							System.out.println("AS TYPE:"+type);
							break out;
						}
					}



					if(clazz.getSuperclass()!=null && !(clazz.getSuperclass() instanceof NoType)) {
//						System.out.println("extendz:"+clazz.getSuperclass().toString());
						TypeElement extendz_clazz = state.elements.getTypeElement(clazz.getSuperclass().toString());
						java.util.List<Symbol> enclosedElements = null ;

						if(extendz_clazz instanceof Type.ClassType) {
							Type.ClassType tt = ((Type.ClassType) extendz_clazz);
							enclosedElements = tt.asElement().getEnclosedElements() ;
						}
						else if(extendz_clazz instanceof Symbol.ClassSymbol) {
							Symbol.ClassSymbol csym = ((Symbol.ClassSymbol) extendz_clazz);
							enclosedElements = csym.getEnclosedElements() ;
						}
						else
							throw new RuntimeException("Didnt expect type : "+extendz_clazz.getClass());

						for(Element e : enclosedElements) {
//							System.out.printf("FOR %s == %s?\n",e.getSimpleName().toString(),nextField.getIdentifier().toString());
//							System.out.println("checksout? "+e.getSimpleName().toString().equals(nextField.getIdentifier().toString()));
							if(e.getSimpleName().toString().equals(nextField.getIdentifier().toString())) {
								type = removeAnnotatedType(e.asType()) ;
								found = e ;
//								System.out.printf("%s -> %s as type:%s\n",e.getSimpleName().toString(),nextField.getIdentifier(),type);
//								System.out.println("AS TYPE:"+type);
								break out;
							}
						}
					}

					for(TypeMirror interfaze : clazz.getInterfaces()) {
						java.util.List<Symbol> enclosedElements = null ;

						if(interfaze instanceof Type.ClassType) {
							Type.ClassType tt = ((Type.ClassType) interfaze);
							enclosedElements = tt.asElement().getEnclosedElements() ;
						}
						else if(interfaze instanceof Symbol.ClassSymbol) {
							Symbol.ClassSymbol csym = ((Symbol.ClassSymbol) interfaze);
							enclosedElements = csym.getEnclosedElements() ;
						}

						for(Element e : enclosedElements) {
//							System.out.printf("FOR %s == %s?\n",e.getSimpleName().toString(),nextField.getIdentifier().toString());
//							System.out.println("checksout? "+e.getSimpleName().toString().equals(nextField.getIdentifier().toString()));
							if(e.getSimpleName().toString().equals(nextField.getIdentifier().toString())) {
								type = removeAnnotatedType(e.asType()) ;
								found = e ;
//								System.out.printf("%s -> %s as type:%s\n",e.getSimpleName().toString(),nextField.getIdentifier(),type);
//								System.out.println("AS TYPE:"+type);
								break out;
							}
						}
					}

					break out;
				}
			}

     	}

//     	System.out.printf("jctree:%s is of type:%s !\n",tree.toString(),type.toString());
		return removeAnnotatedType(type) ;
	}

	protected static String removeArray(String s) {
		return s.replace("[]","");
	}

	public static Element inScope(JCFieldAccess tree, TreeTranslatorStateful state) {
		//TODO adicionar niveis de indireccao infinitos (actualmente só suporta a.b, apartir dai nao faz nada)
		//Type of element preceding eg.: preceding->System . out<-this
		Element precedingElem = null ;
		if(tree.selected instanceof JCIdent)
			precedingElem = inScope((JCIdent)tree.selected, state) ;
		else if(tree.selected instanceof JCFieldAccess)
			precedingElem = inScope((JCFieldAccess)tree.selected, state) ;
//		else
//			throw new RuntimeException("NOPE") ;

//		System.out.println("\tpreceding elem: "+precedingElem);
//		System.out.println("\tenc elems:"+precedingElem.getKind());
//		System.out.println("\tenc elems:"+precedingElem.getModifiers());
//		System.out.println("\tenc elems:"+precedingElem.getEnclosedElements());
		//Iterate the contents of the preceding element

//		System.out.println("\tfinding preceding type : "+tree.selected);
		TypeMirror precedingType = null ;
//		if(precedingElem!=null)
//			System.out.println(precedingElem.getClass());

		if(precedingElem instanceof Symbol.VarSymbol)  {
			Symbol.VarSymbol vsym = (Symbol.VarSymbol)precedingElem ;
//			DebugVisitor.debug(vsym);
//			System.out.println(vsym);
//			JCTree jctree = AST_Query.convert2JCTree(vsym,state) ;
//			precedingType = AST_Query.findType(jctree,state) ;
			if(vsym!=null && vsym.type!=null) {
				precedingType = (TypeMirror) (vsym.type);
			}
			else
				precedingType = AST_Query.findType(tree.selected,state) ;
		}
		else
			precedingType = AST_Query.findType(tree.selected,state) ;

//		System.out.println("\ttype is: "+precedingType);

		//caso especial para aceder ao campo "length" de um array
		if(precedingType instanceof Type.ArrayType && tree.name.toString().equals("length")) {
//			System.out.println("tre name : "+tree.name);
			Type.ArrayType ta = (Type.ArrayType)precedingType ;
//			DebugVisitor.debug(ta.tsym);

			for(Element e : ((ClassSymbol)(ta.tsym)).members_field.getElements()) {
//				System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
				if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//					System.out.println("\tMATCH!");
					return e ;
				}
			}
		}

		while(precedingType instanceof Type.ArrayType || precedingType instanceof Type.MethodType) {
			if(precedingType instanceof Type.ArrayType) {

				precedingType = ((Type.ArrayType)precedingType).elemtype ;
//				System.out.println("\twhile Type.ArrayType...  : "+precedingType);
			}
			else if(precedingType instanceof Type.MethodType) {

				precedingType = ((Type.MethodType)precedingType).getReturnType() ;
//				System.out.println("\twhile Type.MethodType...  : "+precedingType);
			}

		}

		TypeElement clazz = state.elements.getTypeElement(precedingType.toString());

		if(clazz!=null) {
//			System.out.println("\tclazz: "+clazz.getEnclosedElements());

			for(Element e : clazz.getEnclosedElements()) {
//				System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
				if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//					System.out.println("\tMATCH!");
					return e ;
				}
			}

			TypeElement extendz_clazz = state.elements.getTypeElement(clazz.getSuperclass().toString());
			if(extendz_clazz==null) {
				ClassSymbol csym = (ClassSymbol)clazz ;
//				DebugVisitor.debug(csym);
//				System.out.println(csym.getSuperclass());
//				System.out.println(csym.getSuperclass().asElement());
				if(csym.getSuperclass().asElement()!=null)
					extendz_clazz = state.elements.getTypeElement(csym.getSuperclass().asElement().toString());
			}
//			System.out.println("\textended clazz: "+extendz_clazz);

			//ver tambem da classe que extende !!
			if(extendz_clazz!=null) {
				for(Element e : extendz_clazz.getEnclosedElements()) {
//					System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
					if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//						System.out.println("\tMATCH! "+e);
						return e ;
					}
				}
			}

			//pode ser tambem constantes na interface...
			for(TypeMirror interfaze : clazz.getInterfaces()) {
				TypeElement impl_clazz = state.elements.getTypeElement(((Type.ClassType)interfaze).asElement().toString());
//				System.out.println("\timplemeted clazz: "+((Type.ClassType)interfaze).asElement().toString());

				//ver tambem da classe que extende !!
				for(Element e : impl_clazz.getEnclosedElements()) {
//					System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
					if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//						System.out.println("\tMATCH!");
						return e ;
					}
				}
			}
		}
		//ir la pelo simbolo...
		else {
//			ClassSymbol sym = state.elementsC.getTypeElement(precedingType.toString()) ;
//			System.out.println("\tTSYM: "+sym);
			Type.ClassType tt = null ;
			ClassSymbol csym = null ;
			if(precedingType instanceof Type.ClassType)  {
				tt  = (Type.ClassType)precedingType ;
				csym = (ClassSymbol) tt.tsym;
			}
			else if(precedingType instanceof Type.TypeVar)  {
				Type.TypeVar ttt  = (Type.TypeVar)precedingType ;
//				DebugVisitor.debug(ttt.tsym);
//				DebugVisitor.debug(ttt.tsym.getGenericElement());
				csym = (ClassSymbol) ttt.tsym.getGenericElement();
				tt = (Type.ClassType) ttt.tsym.getGenericElement().asType();
//				DebugVisitor.debug(ttt.tsym.asType());
//				System.out.println(ttt.tsym.getBounds());
//				System.out.println(ttt.tsym.getEnclosedElements());
//				ClassSymbol csym = (TypeVar) tt.tsym;
			}

//			DebugVisitor.debug(csym);
//			System.out.println("\tfields:"+csym.members_field.getElements());

			for(Element e : csym.members_field.getElements()) {
//				System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
				if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//					System.out.println("\tMATCH!");
					return e ;

//					return
				}
			}

//			System.out.println("\textended clazz: "+csym.flatname);
//			System.out.println("\textended clazz: "+csym.getSuperclass());
//			System.out.println("\textended clazz: "+csym.getSuperclass().asElement());
//			System.out.println("\textended clazz: "+csym.getInterfaces());

            if(csym.getSuperclass()!=null && csym.getSuperclass().asElement()!=null) {
				TypeElement extendz_clazz = state.elements.getTypeElement(csym.getSuperclass().asElement().toString());
				//ver tambem da classe que extende !!
				for(Element e : extendz_clazz.getEnclosedElements()) {
//					System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
					if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//						System.out.println("\tMATCH! "+e);
						return e ;
					}
				}
			}



			//pode ser tambem constantes na interface...
			for(TypeMirror interfaze : csym.getInterfaces()) {
				TypeElement impl_clazz = state.elements.getTypeElement(((Type.ClassType)interfaze).asElement().toString());
//				System.out.println("\timplemeted clazz: "+((Type.ClassType)interfaze).asElement().toString());

				//ver tambem da classe que extende !!
				for(Element e : impl_clazz.getEnclosedElements()) {
//					System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
					if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//						System.out.println("\tMATCH!");
						return e ;
					}
				}
			}


//			System.out.println("owner:"+csym.owner.getClass());
//			System.out.println("owner:"+csym.owner);
//			DebugVisitor.debug(csym);
//			for(Element e : csym.owner.members_field.getElements()) {
//				System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
//				if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//					System.out.println("\tMATCH!");
//					return e ;
//
////					return
//				}
//			}
		}

		//java.lang.Object!
		for(Element e : state.elements.getTypeElement("java.lang.Object").getEnclosedElements()) {
//			System.out.printf("\t\tcomparing %s and %s...\n",e.getSimpleName().toString(),tree.getIdentifier().toString());
			if(e.getSimpleName().toString().equals(tree.getIdentifier().toString())) {
//				System.out.println("\tMATCH! "+e);
				return e ;
			}
		}

     	throw new RuntimeException("Didn't find JCSelect : '"+tree+"' in scope!") ; //not found in scope
	}

	/**
	 * 
	 * @param tree
	 * @param state
	 * @return
	 */
	public static Element inScope(JCIdent tree, TreeTranslatorStateful state) {
		String ident = tree.getName().toString() ;
//		System.out.println("name:"+ident);
		return inScope(ident,tree,state);
	}


	public static Element inScope(String ident, JCTree tree, TreeTranslatorStateful state) {
//		String ident = tree.getName().toString() ;
//		System.out.println("searching "+ident);
		List<Element> list = variablesInScope(tree, state) ;

		for(Element e : list){
//			System.out.printf("comparing %s with %s(%s)...\n",ident,e.getSimpleName().toString(),(e.getKind()));
			if(ident.equals(e.getSimpleName().toString())) {
//         		System.out.println("FOUND e:"+ e.getSimpleName().toString());
				return e ;
			}
		}


		//comparing with fullname
		for(Element e : list){
			if(e.getKind()== ElementKind.CLASS) {
				String fullname = ((ClassSymbol)e).fullname.toString() ;
//				DebugVisitor.debug((ClassSymbol)e);
//				System.out.println(e.getClass());
//				System.out.printf("comparing %s with %s(%s)...\n",ident,fullname,(e.getKind()));
				if(ident.equals(fullname)) {
//         			System.out.println("FOUND e:"+ e.getSimpleName().toString());
					return e ;
				}
			}
		}

		//check if its not from java.lang automatic imported packages
//		System.out.println("searchingJAVA "+("java.lang."+ident));
     	for(Element e : list)
     		if(("java.lang."+ident).equals(e.getSimpleName().toString())) {
//         		System.out.println("FOUND IN JAVA.LANG. e:"+ e.getSimpleName().toString());
         		return e ;
     		}


		//CUT MY LIFE INTO PIECES
		//THIS IS MY LAST RESORT
//		for(Element e : list)   {
//			String fullname = ((ClassSymbol)e).fullname.toString() ;
////			System.out.printf("comparing %s with %s(%s)...\n",ident,fullname,(e.getKind()));
//			if(("java.lang."+ident).equals(fullname)) {
////				System.out.println("FOUND IN JAVA.LANG. e:"+ e.getSimpleName().toString());
//				return e ;
//			}
//		}
     			
     	return null ; //not found in scope
	}
	

	public static boolean isInScope(JCIdent tree, TreeTranslatorStateful state) {
		return inScope(tree, state)!=null ;
	}
	
	/**
	 * Returns a list of the variables in scope of this tree
	 * @param tree the tree in scope
	 * @return list of elements that are in the scope of this tree
	 */
	public static List<Element> variablesInScope(JCTree tree, TreeTranslatorStateful state) {
		if(cache_variablesInScope.containsKey(tree))
			return cache_variablesInScope.get(tree) ;

     	JavacScope scp = state.treesC.getScope(state.treesC.getPath(state.jcc, tree)) ;
     	//FIXME null pointer when method body is empty!!!!
     	
     	ListBuffer<Element> elemsInScope = ListBuffer.lb() ;
     	elemsInScope.appendList(recursiveScopeAnalysis(scp,state)) ;
     	
     	//Expand packages in scope that are 'staredImports' and add their classes to list
     	for(Element e : elemsInScope) {
     		if(e.getKind().toString().equalsIgnoreCase("PACKAGE")) {
//     			System.out.println("package "+e.toString()+" contains: "+e.getEnclosedElements());
     			elemsInScope.appendList((List<Element>) e.getEnclosedElements()) ;
     		}
     	}

		cache_variablesInScope.put(tree,elemsInScope.toList()) ;
		return elemsInScope.toList() ;
	}
	public static Map<JCTree, List<Element>> cache_variablesInScope = new HashMap<JCTree, List<Element>>();


	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	protected static List<Element> recursiveScopeAnalysis(JavacScope scp, TreeTranslatorStateful state) {
		ListBuffer<Element> l = new ListBuffer<Element>() ;  
     	
    	for(Element e : scp.getLocalElements())
    		l.append(e) ;

        //add recursively the rest of the outer scopes
        if(scp.getEnclosingScope()!=null) 
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingScope(),state)) ;
        if(scp.getEnclosingClass()!=null)
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingClass(),state)) ;
        if(scp.getEnclosingMethod()!=null)
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingMethod(),state)) ;
        
     	return l.toList() ; 
	}

	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	protected static List<Element> recursiveScopeAnalysis(ExecutableElement scp, TreeTranslatorStateful state) { //enclosing method
		ListBuffer<Element> l = new ListBuffer<Element>() ; 

//     	System.out.println("######################") ;
//     	System.out.println(scp.toString()) ;
     	
    	for(Element e : scp.getEnclosedElements())
    		l.append(e) ;	
    	l.append(scp.getEnclosingElement()) ;
    			 
//        for(Element e : l)
//        	System.out.println("elem in scope:\t"+e+"\t"+e.asType()) ;
//     	System.out.println("######################") ;	
     	
     	return l.toList() ;
	}
	
	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	protected static List<Element> recursiveScopeAnalysis(TypeElement scp, TreeTranslatorStateful state) { //enclosing class
		ListBuffer<Element> l = new ListBuffer<Element>() ;

    	for(Element e : scp.getEnclosedElements())
    		l.append(e) ;	
    	
    	l.append(scp.getEnclosingElement());
    	
//    	System.out.println("######################") ; 
//    	System.out.println(scp.toString()) ;    			 
//        for(Element e : l)
//        	System.out.println("elem in scope:\t"+e+"\t"+e.asType()) ;
//     	System.out.println("######################") ;

		//Adds elements in the classes it extends to the scope aswell

//		System.out.println(" class:" + scp.getClass());
//		ClassSymbol sym = (ClassSymbol)scp ;
//		DebugVisitor.debug(sym);
//		System.out.println("extends:"+scp.getSuperclass());
//		System.out.println("extends class:"+scp.getSuperclass().getClass());
//		System.out.println("getclass:"+scp.getClass());
//		System.out.println("classSymbol:"+scp.toString());

		if(!scp.getSuperclass().toString().equals("java.lang.Object")) {
			String clazzName = removePolymorficParams(scp.getSuperclass().toString()) ;
//			System.out.println("clazzname:"+clazzName);
			try{
				ClassSymbol extendz = findClassSym(clazzName,state);
//				System.out.println("ADDING extendz TO SCOPE:"+extendz);

				l.appendList(recursiveScopeAnalysis((((TypeElement)extendz)),state)) ;
			}catch(Exception e) {}
		}

//		if(tree instanceof JCClassDecl) {
//
//		}
     	
     	return l.toList() ;
	}

	public static String removePolymorficParams(String type) {
		return type.replaceAll("\\<(.*)\\>","");
	}

	public static List<TypeMirror> findType(List<JCTree> l, TreeTranslatorStateful state) {
		ListBuffer<TypeMirror> lb = ListBuffer.lb() ;
		
		for(JCTree tree : l) {
			TypeMirror type ;
			if(tree instanceof JCFieldAccess)
				type = findType((JCFieldAccess) tree, state) ;
			else if(tree instanceof JCVariableDecl)
				type = findType((JCVariableDecl) tree, state) ;
			else 
				throw new RuntimeException("NOT PROGRAMMED FOR JCTREE TYPE: "+tree.getClass());
			
			lb.add(type);
		}
		
		return lb.toList();
	}

	public static Element convert2Element(JCTree tree, TreeTranslatorStateful state) {

		Element eC = state.treesC.getElement(state.trees.getPath(state.jcc, tree)) ;
		Element e = state.trees.getElement(state.trees.getPath(state.jcc, tree)) ;
		if(e!=eC || (e!=null && eC!=null && !eC.toString().equalsIgnoreCase(e.toString())))
			throw new RuntimeException("TreeC and Tree didn't return same element... e:"+e.toString()+" eC:"+eC.toString()) ;
		return eC ;
	}


	public static Element convert2Element(JCTree tree, TreeScannerStateful state) {

		Element eC = state.treesC.getElement(state.trees.getPath(state.jcc, tree)) ;
		Element e = state.trees.getElement(state.trees.getPath(state.jcc, tree)) ;
		if(e!=eC || (e!=null && eC!=null && !eC.toString().equalsIgnoreCase(e.toString())))
			throw new RuntimeException("TreeC and Tree didn't return same element... e:"+e.toString()+" eC:"+eC.toString()) ;
		return eC ;
	}
	
	public static JCTree convert2JCTree(Element e, TreeTranslatorStateful state) {
     	return state.elementsC.getTree(e);
	}
 
	public static Set<javax.lang.model.element.Modifier> getModifiers(JCFieldAccess tree, TreeTranslatorStateful state) {
		Element e = convert2Element(tree, state);
		if(e!=null) { //present on compilation unit AST, easy!
			return e.getModifiers();
		}
		else { //not present on compilation unit (.class)...
			
		} 
//		System.out.println();
		return null ;
	}
	

	public static boolean isField(JCFieldAccess field, TreeTranslatorStateful state) {
//		System.out.printf("#### isField(%s)\n",field.toString());
//		System.out.println("\tkind:"+field.getKind());
//		System.out.println("\ttag:"+field.getTag());
		
		Element e = inScope(field, state);
//		System.out.println("\telemnt:"+e.toString());
//		System.out.println("\tkinds:"+e.getKind());
//		System.out.println("\tmods:"+e.getModifiers());
		
		//classe que representa 
//     	TypeMirror type = findType(field, state) ;
//     	System.out.println("\ttype:"+type.toString());
     	 
     	//If class FIELD
     	return e.getKind()==javax.lang.model.element.ElementKind.FIELD;
	}

	public static boolean isField(JCIdent ident, TreeTranslatorStateful state) {
		//FIXME nao sei se devo considerar super um field ou nao... o java considera mas...
		//	por enquanto, e para compatibilidade, vou ignorar
		if(ident.toString().equals("super"))
			return false ;

		//if an array type, it's not a field
		if(ident.toString().contains("[]"))
			return false ;            //FIXME isto tá mal de certeza...

//		System.out.printf("#### isField(%s)\n",ident.toString());
//		System.out.println("\tkind:"+ident.getKind());
//		System.out.println("\ttag:"+ident.getTag());
		
		Element e = inScope(ident, state);
//		System.out.println("\telemnt:"+e.toString());
//		System.out.println("\tkinds:"+e.getKind());
//		System.out.println("\tmods:"+e.getModifiers());
		
		//classe que representa 
//     	TypeMirror type = findType(ident, state) ;
//     	System.out.println("\ttype:"+type.toString());

		if(e==null) { //again, start() falls on this...
			return false ; //so, not a field
		}

     	//If class FIELD
     	return e.getKind()==javax.lang.model.element.ElementKind.FIELD;
	}
	
	public static boolean hasModifier(JCExpression e, String modifier, TreeTranslatorStateful state) {
		if(e instanceof JCIdent)
			return hasModifier((JCIdent)e, modifier, state);
		else if(e instanceof JCFieldAccess)
			return hasModifier((JCFieldAccess)e, modifier, state);
		else
			throw new RuntimeException("e:"+e.toString()+" is not an JCIdent as expected.");
	}


	protected static boolean hasModifier(JCFieldAccess field, String modifier, TreeTranslatorStateful state) {
		//FIXME nao sei se devo considerar super um field ou nao... o java considera mas...
		//	por enquanto, e para compatibilidade, vou ignorar
//		if(ident.toString().equals("super") && modifier.equalsIgnoreCase("protected"))
//			return false ;
		//FIXME estou a considerar o "this" um field privado...
		//Edge cases
		if(field.toString().equals("this")) {
			if(modifier.equalsIgnoreCase("protected"))
				return true ;
			if(modifier.equalsIgnoreCase("public"))
				return false ;
		}


		Element e = inScope(field, state);
		if(e==null)
			throw new RuntimeException("Element in scope is null, not expected!");

//		System.out.println("elemnt:"+e.toString());
//		System.out.println("\tkinds:"+e.getKind());
//		System.out.println("\tmods:"+e.getModifiers());
//		System.out.println("\tis "+modifier+"?"+containsModifier(e, modifier));

		//TODO falta ver protected aqui? posso ser uma classe extendida...
		return containsModifier(e, modifier) ;
	}

	protected static boolean hasModifier(JCIdent ident, String modifier, TreeTranslatorStateful state) {
		//FIXME nao sei se devo considerar super um field ou nao... o java considera mas...
		//	por enquanto, e para compatibilidade, vou ignorar
//		if(ident.toString().equals("super") && modifier.equalsIgnoreCase("protected"))
//			return false ;
		//FIXME estou a considerar o "this" um field privado...
		//Edge cases
		if(ident.toString().equals("this")) {
			if(modifier.equalsIgnoreCase("protected")) 
				return true ;
			if(modifier.equalsIgnoreCase("public"))
				return false ;
		}
			

		Element e = inScope(ident, state);
		if(e==null)
			throw new RuntimeException("Element in scope is null, not expected!");

//		System.out.println("elemnt:"+e.toString());
//		System.out.println("\tkinds:"+e.getKind());
//		System.out.println("\tmods:"+e.getModifiers());
//		System.out.println("\tis "+modifier+"?"+containsModifier(e, modifier));

		//TODO falta ver protected aqui? posso ser uma classe extendida...
		return containsModifier(e, modifier) ;
	}
	
	/**
	 * Possible values from javax.lang.model.element.Modifier:
	 * 
	 * The modifier {@code public}           PUBLIC,
     * The modifier {@code protected}        PROTECTED,
     * The modifier {@code protected}          protected,
     * The modifier {@code abstract}         ABSTRACT,
     * The modifier {@code static}           STATIC,
     * The modifier {@code final}            FINAL,
     * The modifier {@code transient}        TRANSIENT,
     * The modifier {@code volatile}         VOLATILE,
     * The modifier {@code synchronized}     SYNCHRONIZED,
     * The modifier {@code native}           NATIVE,
     * The modifier {@code strictfp}         STRICTFP;
     * 
	 * @param e
	 * @param modifier
	 * @return
	 */
	public static boolean containsModifier(Element e, String modifier) {			
		for(javax.lang.model.element.Modifier each : e.getModifiers()) {
			if(each.toString().equalsIgnoreCase(modifier))
				return true ;
		}
		return false ; //if not found
	}

	public static String getClassFullName(JCClassDecl clazz, TreeTranslatorStateful state) {
		TypeMirror clazzName = AST_Query.convert2Element(clazz, state).asType();
		return clazzName.toString();
	}

	public static String getClassFullName(JCClassDecl clazz, TreeScannerStateful state) {
		TypeMirror clazzName = AST_Query.convert2Element(clazz, state).asType();
		return clazzName.toString();
	}


	public static JCClassDecl findClass(ClassSymbol sym, TreeTranslatorStateful state) {
		JCTree tree = state.elementsC.getTree(sym);
		if(tree==null)
			throw new RuntimeException("Class name:"+sym.flatname+" not present on compilation unit!");
		if(!(tree instanceof JCClassDecl))
			throw new RuntimeException("Class name:"+sym.flatname+" given is not a JCClassDecl, but a "+tree.getClass()+"?!");
		return (JCClassDecl) tree ;
	}

	public static JCClassDecl findClass(String fullname, TreeTranslatorStateful state) {
		ClassSymbol sym = state.elementsC.getTypeElement(fullname);
		return findClass(sym,state);
	}

	public static ClassSymbol findClassSym(String fullname, TreeTranslatorStateful state) {
		ClassSymbol sym = state.elementsC.getTypeElement(fullname);
		JCTree tree = state.elementsC.getTree(sym);
		if(tree==null)
			throw new RuntimeException("Class name:"+fullname+" not present on compilation unit!");
		if(!(tree instanceof JCClassDecl))
			throw new RuntimeException("Class name:"+fullname+" given is not a JCClassDecl, but a "+tree.getClass()+"?!");
		return sym ;
	}

//	public static boolean done = false;
//	public static JCVariableDecl createNewGlobalPartition(String partName, JCClassDecl clazz, TreeTranslatorStateful state) {
//		if(done)
//			return null;
//		String staticPartitions = GenerateGlobalPartitionsFilePhase.globalPartitionPackageName+"."+GenerateGlobalPartitionsFilePhase.globalPartitionClassName ;
//		System.out.println("class with global partitions:"+staticPartitions);
//
//		JCClassDecl partitions = findClass(staticPartitions, state);
//		DebugVisitor.debug(partitions);
//
//		ListBuffer<JCTree> defs = new ListBuffer<>();
//		defs.addAll(partitions.defs) ;
//
//
//		JCVariableDecl partition = AST_Query.createPartition(partName,partitions,clazz,state);
//
////		System.out.println("INIT:" + partition.init);
////		System.out.println("SYM:"+partition.sym);
////		DebugVisitor.debug(partition);
//		//defs.add(partition);
//
//		//partitions.defs = defs.toList();
//		//partition.accept(state.memberEnter);
////		System.out.println("new-sym.flags:" + partition.sym.flags_field);
////		System.out.println("new-sym:"+partition.sym);
//		//partitions.accept(state.memberEnter);
//
//		//System.out.println("1051:"+(partition.sym));
////		for(Element e : variablesInScope(partition,state))
////			System.out.println("e:"+e);
////		System.exit(0);
//
//		done = true;
//		return null;
//	}



	/**
	 * Returns partition declaration with it's type default constructor.
	 * @return
	 */
	public static JCVariableDecl createPartition(String partName, TreeTranslatorStateful state) {
//		JCExpression vartype = AST_Query.typeFromString("java.util.concurrent.locks.ReentrantLock", state);
		JCExpression vartype = AST_Query.typeFromString("ReentrantLock", state);
		JCExpression init = AST_Query.emptyConstructor("ReentrantLock", state);
		JCTree.JCModifiers mods = state.make.Modifiers(Modifier.PUBLIC | Modifier.STATIC);// | Modifier.FINAL) ;
		JCVariableDecl var = state.make.VarDef(mods, state.names.fromString(partName), vartype, init);
		//findClass("java.util.concurrent.locks.ReentrantLock") ;
		//var.sym = new Symbol.VarSymbol(262153, state.names.fromString("partName"), e.t, owner.sym);

//		System.out.println("owner sym:"+partitions.sym.owner.toString());
//		                                Element e = convert2Element(partitions,state);
		//System.out.println("l:"+l.size());

//		JavacScope scp = state.treesC.getScope(state.treesC.getPath()) ;


//		System.exit(1);
//		Element e = inScope(owner., state);
//		System.out.println("e:"+e);

		return var ;
	}

	public static String getClassShortName(TypeMirror type) {
		if(type instanceof Type.ClassType) {
			ClassSymbol tsym = (ClassSymbol) ((Type.ClassType)type).tsym;
//			DebugVisitor.debug(tsym);

			//Internal class, so use TOP.INNER for the short class name
			if(tsym.getNestingKind()== NestingKind.MEMBER) {
				String concat = getClassShortName(tsym.owner.type)+"."+tsym.name.toString() ;
//				System.out.println("result:"+concat);
				return concat;
			}
			else
				return tsym.name.toString();
		}
		else if(type instanceof Type.ArrayType) {
			Type t = ((Type.ArrayType)type).elemtype;
//			DebugVisitor.debug(t);
			return getClassShortName((TypeMirror) t);
		}
		else if(type instanceof Type) {
			Type t = (Type)type ;
//			DebugVisitor.debug((Type)type);
//			System.out.println(type);
//			DebugVisitor.debug(t.tsym);
			return t.tsym.name.toString();
		}
		throw new RuntimeException("Type '"+type.toString()+"' wasn't expected class, but '"+type.getClass()+"' instead.") ;
	}

	public static String getRawType(TypeMirror type, TreeTranslatorStateful state) {
		type = removeAnnotatedType(type) ;
		if(type instanceof Type.ArrayType)
		{
			Type t = ((Type.ArrayType) type).elemtype;
			while(t instanceof Type.ArrayType)
				t = ((Type.ArrayType) t).elemtype ;

			String elemType = t.asElement().toString() ;

//			System.out.println("RIGHT TYPE:"+elemType);
			return elemType ;
		}
		else if(type instanceof Type.ClassType)
		{
			return type.toString() ;
		}
		throw new RuntimeException("NOT IMPLEMENTED GET RAW TYPE FOR CLASS INSTANCE -> "+type.getClass())  ;
	}

	public static Type getMethodReturnType(JCMethodDecl method, TreeTranslatorStateful state) {
		JCExpression retType = method.restype ;
//		System.out.println("rettype:"+retType.type);
		try {
			if(retType instanceof JCPrimitiveTypeTree)
				return (Type) state.typesC.getPrimitiveType(((JCPrimitiveTypeTree) retType).getPrimitiveTypeKind());
			if(retType instanceof JCArrayTypeTree)
				return (Type) state.typesC.getArrayType(((JCArrayTypeTree) retType).elemtype.type);
//			if(retType instanceof JCIdent )
//				return state.elementsC.getTypeElement(((JCIdent) retType).name.toString());
			if(retType.type!=null)
				return retType.type;
			else
				return state.elementsC.getTypeElement(retType.toString()).type;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldnt find return type("+retType.getClass()+")='"+retType.toString()+"' of method "+method.name.toString()) ;
		}
	}

	public static JCBlock expr2JCBlock(JCStatement stat, TreeScannerStateful state) {

		long flags = 0 ; //??? maybe? looks like it
		return state.make.Block(flags,List.of(stat));
	}

	/**
	 * Used on the body of ifs to add enclosing JCBlock to statements if there is no enclosing JCBlock
	 * @param tree
	 * @param state
	 * @return
	 */
	public static JCBlock blockify(JCStatement tree, TreeScannerStateful state) {
		if(!(tree instanceof JCBlock)) {
			return AST_Query.expr2JCBlock(tree,state);
		}
		else
			return (JCBlock)tree ;

//		DebugVisitor.debug(tree);
//		throw new RuntimeException("Body of if statement '"+tree.toString()+"' was not JCBlock or JCExpressionStatement/JCContinue, aborting!");
	}

	/**
	 * Also contemplates m,ethods attained from extending!
	 *
	 * @param fullname
	 * @param state
	 * @return
	 */
	public static List<JCMethodDecl> getClassMethods(String fullname, TreeTranslatorStateful state) {
		ListBuffer<JCMethodDecl> methods = ListBuffer.lb();

		JCClassDecl clazz = findClass(fullname, state);
		while(clazz!=null) {
//			System.out.println("clazz==null?"+(clazz==null));
			for(JCTree t : clazz.defs) {
				if(t instanceof  JCMethodDecl)
					methods.add((JCMethodDecl) t);
			}
//			System.out.println("extendi9ng:"+clazz.extending.getClass().toString());
//			System.out.println("extendi9ng:" + clazz.extending.toString());
			JCClassDecl nextclazz = null ;
			if(clazz.extending!=null && clazz.extending.type!=null && clazz.extending.type.tsym!=null) {
				ClassSymbol csym = (ClassSymbol) clazz.extending.type.tsym;
				nextclazz = AST_Query.findClass(csym,state);
//				System.out.println("FOUND CLAZZ");
			}
//			DebugVisitor.debug(csym);
//			System.out.println("END clazz==null?"+(nextclazz==null));
			clazz = nextclazz ;
		}

//		System.out.println("ASKLDJLAKSJDLKSAJDKLAS");
//		System.out.println("methods found:"+methods.toList().toString());
		return methods.toList();
	}

	public static boolean canEscapeBranch(JCIf iff, TreeTranslatorStateful state) {
		if(iff.elsepart==null)
			return true ;

		JCStatement elseee = iff.elsepart ;
		if(elseee instanceof JCBlock)
			return false ;

		System.out.println(elseee.toString());
		System.out.println(elseee.getClass());
		DebugVisitor.debug(elseee);
		throw new RuntimeException("Have to implement how to escape from this branch!");
	}

	/**
	 * Checks if a given statement makes use of any of the variables passed as argument.
	 *
	 * @param stat - statement to check
	 * @param lvars - list of variables to check for reference
	 * @return false if no variable is used in this stamement, true otherwise
	 */
	public static boolean referencesVars(JCStatement stat, List<JCTree> vars) {
		for(JCTree var : vars) {
			if(referencesVars(stat,var))
				return true ;
		}

		return false;  //no reference found
	}

	public static boolean referencesVars(JCStatement stat, java.util.List<JCVariableDecl> params, java.util.List<JCExpression> fields, java.util.List<JCExpression> localVars) {
		List<JCTree> vars = ListUtil.merge(params,fields,localVars) ;
		return referencesVars(stat,vars) ;
	}

	public static boolean referencesVars(JCStatement stat, JCTree var) {
		final boolean[] found = {false};
//		if(true)
//			return false ;

		if(var instanceof JCTree.JCIdent || var instanceof JCVariableDecl) {
			final Name varName = (var instanceof JCIdent) ? ((JCIdent) var).name : ((JCVariableDecl) var).name ;

			stat.accept(new TreeScanner() {
				@Override
				public void visitIdent(JCTree.JCIdent tree) {
					if (tree.name.toString().equals(varName.toString())) {
//						System.out.println("FOUND " + var.toString() + " IN " + stat.toString());
						found[0] = true;
					}
					super.visitIdent(tree);
				}
			});
		}
		else {
			final String varName = ((JCFieldAccess)var).toString() ;

			stat.accept(new TreeScanner() {
				@Override
				public void visitSelect(JCFieldAccess tree) {
					if (tree.name.toString().equals(varName)) {
//						System.out.println("FOUND " + var.toString() + " IN " + stat.toString());
						found[0] = true;
					}
					super.visitSelect(tree);
				}
			});
//			System.out.println("stat:"+stat.toString());
//			DebugVisitor.debug(var);
//			throw new RuntimeException("Don't know how to handle this class :"+var.getClass()) ;
		}


		return found[0] ;
	}


}
