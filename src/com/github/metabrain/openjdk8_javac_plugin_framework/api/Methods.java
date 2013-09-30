package com.github.metabrain.openjdk8_javac_plugin_framework.api;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class Methods {
	

	/** Arguments = "(" [Expression { COMMA Expression }] ")"
     1427      */
//     1428     List<JCExpression> arguments() {
//     1429         ListBuffer<JCExpression> args = lb();
//     1430         if (token.kind == LPAREN) {
//     1431             nextToken();
//     1432             if (token.kind != RPAREN) {
//     1433                 args.append(parseExpression());
//     1434                 while (token.kind == COMMA) {
//     1435                     nextToken();
//     1436                     args.append(parseExpression());
//     1437                 }
//     1438             }
//     1439             accept(RPAREN);
//     1440         } else {
//     1441             syntaxError(token.pos, "expected", LPAREN);
//     1442         }
//     1443         return args.toList();
//     1444     }

	//Create a list of arguments that is empty
	public static List<JCExpression> noArg() {
		return args();
	}
	
	//Create a list of arguments that is empty
	public static List<JCExpression> args(JCExpression... es) {
		ListBuffer<JCExpression> args = ListBuffer.lb() ; //lb() is empty list constructor
		
		//Add each argument to the list, in order
		for(JCExpression e : es) {
			args.add(e) ;
		}
		
		return args.toList();
	}
	
//	public static void List
	
//	public static JCMethodInvocation invoke(String funName, Names names, )
	
	
}
