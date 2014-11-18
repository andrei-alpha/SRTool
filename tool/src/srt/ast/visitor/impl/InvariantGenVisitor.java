package srt.ast.visitor.impl;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Invariant;
import srt.ast.WhileStmt;

public class InvariantGenVisitor extends DefaultVisitor {


	public InvariantGenVisitor() {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		HashSet<String> modifies = whileStmt.getModifies();
		HashSet<String> uses = whileStmt.getUses();
		
		ArrayList<Integer> properties = new ArrayList<Integer>();
		properties.add(BinaryExpr.GEQ);
		properties.add(BinaryExpr.NEQUAL);
		properties.add(BinaryExpr.GT);
		properties.add(BinaryExpr.EQUAL);
		properties.add(BinaryExpr.LEQ);
		properties.add(BinaryExpr.LT);
		
		DeclRef zero = new DeclRef("zero", 0);
//		DeclRef bound = new DeclRef("bound", whileStmt.getBound().getValue());
//		DeclRef loopcount = new DeclRef("loopCount", whileStmt.getLoopCount());
		
		System.out.println("Before adding candidates, number of Invariants for WhileStmt \n" +
				whileStmt.getInvariantList().getSize());
		
		for (String var1 : modifies) {
			for (int operator : properties) {
				for (String var2 : uses) {
					DeclRef decl1 = new DeclRef(var1);
					DeclRef decl2 = new DeclRef(var2);
					Invariant inv = new Invariant(true, new BinaryExpr(operator, decl1, decl2));
					whileStmt.addToInvariantList(inv);
				}
//				DeclRef decl1 = new DeclRef(var1);
//				Invariant inv1 = new Invariant(true, new BinaryExpr(operator, decl1, zero));
//				whileStmt.addToInvariantList(inv1);
			}
		}
		
//		for (String var1 : modifies) {
//			for (String var2 : modifies) {
//				for (int operator : properties) {
//					DeclRef decl1 = new DeclRef(var1);
//					DeclRef decl2 = new DeclRef(var2);
//					Invariant inv = new Invariant(true, new BinaryExpr(operator, decl1, decl2));
//					whileStmt.addToInvariantList(inv);
//				}
//			}
//		}
		
		System.out.println("After adding candidates, number of Invariants for WhileStmt \n" +
				whileStmt.getInvariantList().getSize());
		return whileStmt;
	}
}
