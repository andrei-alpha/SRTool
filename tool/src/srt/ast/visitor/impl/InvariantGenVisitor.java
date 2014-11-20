package srt.ast.visitor.impl;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.*;
import srt.tool.CollectConstraintsVisitor;

public class InvariantGenVisitor extends DefaultVisitor {

	private HashSet<String> assertUses;
	private ArrayList<Integer> values;
	CollectConstraintsVisitor collectConstraintsVisitor;

	public InvariantGenVisitor() {
		super(true);
		assertUses = new HashSet<String>();
		values = new ArrayList<Integer>();
		collectConstraintsVisitor = new CollectConstraintsVisitor();
	}

	@Override
	public Object visit(Program program) {
		collectConstraintsVisitor.visit(program);
		return visitChildren(program);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		HashSet<String> modifies = whileStmt.getModifies();
		HashSet<String> uses = whileStmt.getUses();

		// Add all loop assertion variables to assertUses
		// Add all loop assertion constants to values
		for (AssertStmt assertStmt : collectConstraintsVisitor.propertyNodes) {
			Expr conditionExpr = assertStmt.getCondition();
			extractAssertionLiterals(assertUses, values, conditionExpr);
		}
		extractAssertionLiterals(assertUses, values, whileStmt.getCondition());

		// Add candidate invariants in respect to certain values
		if (!values.contains(0)) {
			values.add(0);
		}
		
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
			}
		}

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

		// Add candidate invariants in respect to variables in assertions
		for (String var1 : assertUses) {
			for (String var2 : assertUses) {
				for (int operator : properties) {
					if (var1.equals(var2))
						continue;
					DeclRef decl1 = new DeclRef(var1);
					DeclRef decl2 = new DeclRef(var2);
					Invariant inv = new Invariant(true, new BinaryExpr(operator, decl1, decl2));
					whileStmt.addToInvariantList(inv);
				}
			}
		}
		return whileStmt;
	}

	private void extractAssertionLiterals(HashSet<String> assertUses,
										  ArrayList<Integer> values, Expr e) {
		if (e instanceof UnaryExpr) {
			UnaryExpr uExpr = (UnaryExpr) e;
			extractAssertionLiterals(assertUses, values, uExpr.getOperand());
		} else if (e instanceof BinaryExpr) {
			BinaryExpr biExpr = (BinaryExpr) e;

			if (isSimpleBinaryExpr(biExpr)) {
				// Add all DeclRefs in the simple binary expressions
				assertUses.addAll(biExpr.getUses());

				// Add all constants in the simple binary expresisons
				addIntLiteralToValues(values, biExpr.getLhs());
				addIntLiteralToValues(values, biExpr.getRhs());
			} else {
				extractAssertionLiterals(assertUses, values, biExpr.getLhs());
				extractAssertionLiterals(assertUses, values, biExpr.getRhs());
			}
		}
	}

	private boolean isSimpleBinaryExpr(BinaryExpr expr) {
		return (isIntLiteralOrDeclRef(expr.getLhs()) &&
				isIntLiteralOrDeclRef(expr.getRhs()));
	}

	private boolean isIntLiteralOrDeclRef(Expr e) {
		return (e instanceof IntLiteral || e instanceof DeclRef);
	}

	private void addIntLiteralToValues(ArrayList<Integer> values, Expr e) {
		if (e instanceof IntLiteral) {
			int value = ((IntLiteral) e).getValue();
			if (!values.contains(value)) {
				values.add(value);
			}
		}
	}
}
