package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.*;
import srt.ast.visitor.impl.DefaultVisitor;

public class InvariantGenVisitor extends DefaultVisitor {

	private HashSet<String> assertUses;
	private HashSet<Integer> values;
	CollectConstraintsVisitor collectConstraintsVisitor;

	public InvariantGenVisitor() {
		super(true);
		assertUses = new HashSet<String>();
		values = new HashSet<Integer>();
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
			extractAssertionLiterals(conditionExpr);
		}
		extractAssertionLiterals(whileStmt.getCondition());

		// Add common boundary constants to values set
		values.add(0);
		values.add(1);
		values.add(-1);
		
		// Add candidate invariant for every two pairs of variables
		ArrayList<Integer> properties = getComparingOperators();

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

		// Add candidate invariant for comparing modified variables with assertion constants
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
		ArrayList<Integer> oneWayProperties = getOneWayComparingOperators();
		for (String var1 : assertUses) {
			for (String var2 : assertUses) {
				for (int operator : oneWayProperties) {
					if (var1.equals(var2))
						continue;
					DeclRef decl1 = new DeclRef(var1);
					DeclRef decl2 = new DeclRef(var2);
					Invariant inv = new Invariant(true, new BinaryExpr(operator, decl1, decl2));
					whileStmt.addToInvariantList(inv);
				}
			}
		}

		// Add candidate invariants to check even / odd of modifies
		for (String var : modifies) {
			DeclRef decl = new DeclRef(var);
			IntLiteral zero = new IntLiteral(0);
			IntLiteral one = new IntLiteral(1);
			IntLiteral two = new IntLiteral(2);
			BinaryExpr mod = new BinaryExpr(BinaryExpr.MOD, decl, two);
			Invariant even = new Invariant(true, new BinaryExpr(BinaryExpr.EQUAL, mod, zero));
			Invariant odd = new Invariant(true, new BinaryExpr(BinaryExpr.EQUAL, mod, one));
			whileStmt.addToInvariantList(even);
			whileStmt.addToInvariantList(odd);
		}

		return whileStmt;
	}

	private void extractAssertionLiterals(Expr e) {
		if (e instanceof UnaryExpr) {
			UnaryExpr uExpr = (UnaryExpr) e;
			extractAssertionLiterals(uExpr.getOperand());
		} else if (e instanceof BinaryExpr) {
			BinaryExpr biExpr = (BinaryExpr) e;
			if (isSimpleBinaryExpr(biExpr)) {
				// Add all DeclRefs in the simple binary expressions
				assertUses.addAll(biExpr.getUses());

				// Add all constants in the simple binary expresisons
				addIntLiteralToValues(values, biExpr.getLhs());
				addIntLiteralToValues(values, biExpr.getRhs());
			} else {
				extractAssertionLiterals(biExpr.getLhs());
				extractAssertionLiterals(biExpr.getRhs());
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

	private void addIntLiteralToValues(HashSet<Integer> values, Expr e) {
		if (e instanceof IntLiteral) {
			int value = ((IntLiteral) e).getValue();
			values.add(value);
		}
	}

	private ArrayList<Integer> getOneWayComparingOperators() {
		ArrayList<Integer> properties = new ArrayList<Integer>();
		properties.add(BinaryExpr.GEQ);
		properties.add(BinaryExpr.NEQUAL);
		properties.add(BinaryExpr.GT);
		properties.add(BinaryExpr.EQUAL);
		return properties;
	}

	private ArrayList<Integer> getComparingOperators() {
		ArrayList<Integer> properties = getOneWayComparingOperators();
		properties.add(BinaryExpr.LEQ);
		properties.add(BinaryExpr.LT);
		return properties;
	}

 }
