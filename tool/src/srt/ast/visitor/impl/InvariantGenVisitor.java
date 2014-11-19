package srt.ast.visitor.impl;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.IntLiteral;
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
		
		// Add candidate invariant for every two pairs of variables
		ArrayList<Integer> properties = new ArrayList<Integer>();
		properties.add(BinaryExpr.GEQ);
		properties.add(BinaryExpr.NEQUAL);
		properties.add(BinaryExpr.GT);
		properties.add(BinaryExpr.EQUAL);
		properties.add(BinaryExpr.LEQ);
		properties.add(BinaryExpr.LT);
		
		for (String var1 : modifies) {
			for (int operator : properties) {
				for (String var2 : uses) {
					if (var1.equals(var2))
						continue;
					
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
		
		// Add candidate invariants in respect to certain values
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(0);
		for (String var1 : modifies) {
			for (int operator : properties) {
				for (int value : values) {
					DeclRef decl = new DeclRef(var1);
					IntLiteral intl = new IntLiteral(value);
					Invariant inv = new Invariant(true, new BinaryExpr(operator, decl, intl));
					whileStmt.addToInvariantList(inv);
				}
			}
		}

		return whileStmt;
	}
}
