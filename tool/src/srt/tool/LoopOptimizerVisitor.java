package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BinaryOperator;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Node;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.FunctionUtil;

public class LoopOptimizerVisitor extends DefaultVisitor {
	private ArrayList<String> vars;
	private ArrayList<Expr> exprs;
	private ArrayList<Boolean> containsX;
	private Expr iteratorsCountExpr;
	private Expr delta;
	private boolean expectX;
	
	public LoopOptimizerVisitor() {
		super(true);
	}
	
	@Override
	public Object visit(WhileStmt whileStmt) {
		// Try to simply any children first
		whileStmt = (WhileStmt) super.visit(whileStmt);
		
		vars = new ArrayList<String>();
		exprs = new ArrayList<Expr>();
		containsX = new ArrayList<Boolean>();
		boolean canSimply = true;
		canSimply &= analyzeLoop(whileStmt, whileStmt);
		canSimply &= analyzeCondition(whileStmt, whileStmt.getCondition());
		
		if (!canSimply)
			return whileStmt;
		
		// Nice, we can simply this loop then
		ArrayList<Stmt> stmts = new ArrayList<Stmt>();
		
		DeclRef iterationsCount = new DeclRef("cnt$");
		AssignStmt iterations = new AssignStmt(iterationsCount,
				new BinaryExpr(BinaryExpr.DIVIDE, iteratorsCountExpr, delta));
		stmts.add(iterations);
		
		for (int i = 0; i < vars.size(); ++i) {
			String varName = vars.get(i);
			Expr expr = exprs.get(i);
			boolean contains = containsX.get(i);
			
			// this was the expr for iterator
			if (varName == null)
				continue;
			
			DeclRef declRef = new DeclRef(varName);
			BinaryExpr binaryExpr = new BinaryExpr(BinaryExpr.MULTIPLY, iterationsCount, expr);
			if (contains) {
				binaryExpr = new BinaryExpr(BinaryExpr.ADD, declRef, binaryExpr);
			}
			AssignStmt assignStmt = new AssignStmt(declRef, binaryExpr);
			stmts.add(assignStmt);
		}
		
		BlockStmt blockStmt = new BlockStmt(stmts);
		return super.visit(blockStmt);
	}
	
	// Check the loop condition is applied on only one variable
	private boolean analyzeCondition(WhileStmt whileStmt, Expr expr) {
		if (!(expr instanceof BinaryExpr))
			return false;
		BinaryExpr binaryExpr = (BinaryExpr) expr;
		Expr lhs = binaryExpr.getLhs();
		Expr rhs = binaryExpr.getRhs();
		int operator = binaryExpr.getOperator();
		
		// Let's assume lhs has the var in mod set and rhs is constant
		if (FunctionUtil.setIntersection(lhs.getUses(), whileStmt.getModifies())) {
			lhs = binaryExpr.getRhs();
			rhs = binaryExpr.getLhs();
			operator = BinaryExpr.reverseOperator(binaryExpr.getOperator());
		}
		if (!(lhs instanceof DeclRef) || FunctionUtil.setIntersection(rhs.getUses(), whileStmt.getModifies()))
			return false;
		DeclRef iterator = (DeclRef) lhs;
		
		delta = getIteratorExpr(iterator.getName());
		IntLiteral justOne = new IntLiteral(1);
		if (delta == null)
			return false;
		
		// Can handle only these operators
		Expr iterationsExpr = null;
		if (operator == BinaryExpr.GEQ) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, iterator, 
					new BinaryExpr(BinaryExpr.ADD, rhs, justOne));
		} else if (operator == BinaryExpr.GT) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, iterator, rhs);		
		} else if (operator == BinaryExpr.LEQ) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, rhs,
					new BinaryExpr(BinaryExpr.ADD, iterator, justOne));
		} else if (operator == BinaryExpr.LT) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, rhs, iterator);		
		}
		
		if (iterationsExpr != null) {
			iteratorsCountExpr = new BinaryExpr(BinaryExpr.DIVIDE, iterationsExpr, delta);
			return false;
		}
		
		return false;
	}
	
	private Expr getIteratorExpr(String varName) {
		for (int i = 0; i < vars.size(); ++i) {
			if (vars.get(i).equals(varName)) {
				Expr expr = exprs.get(i);
				boolean containsIterator = containsX.get(i);
				if (expr instanceof BinaryExpr) {
					// if i is always set to a constant return
					if (!containsIterator)
						return null;
						
					// delete varname from list
					vars.set(i, null);
					
					// i needs to be in a binary expr, can't handle unary expr
					BinaryExpr binaryExpr = (BinaryExpr) expr;
					return binaryExpr;
				}
			}
		}
		return null;
	}
	
	// Check the loop doesn't contain if, while, asserts or assumes on loop modset
	private boolean analyzeLoop(WhileStmt whileStmt, Node node) {
		boolean result = true;
		HashSet<String> modifies = whileStmt.getModifies();
		
		if (node instanceof WhileStmt)
			return false;
		
		// Will handle if cases later if they don't contain condition on modset
		if (node instanceof IfStmt)
			return false;
		if (node instanceof AssumeStmt && !FunctionUtil.setIntersection(modifies, node.getUses()))
			return false;
		if (node instanceof AssertStmt && !FunctionUtil.setIntersection(modifies, node.getUses()))
			return false;
		if (node instanceof BlockStmt) {
			for (Node child : ((BlockStmt) node).getStmtList().getStatements())
				result &= analyzeLoop(whileStmt, child);
		}
		
		// The tricky case, enforce no assignment from vars in modifies
		if (node instanceof AssignStmt) {
			String varName = ((AssignStmt) node).getLhs().getName();
			HashSet<String> uses = node.getModifies();
			HashSet<String> usesAndModifies = FunctionUtil.getIntersection(modifies, uses);
			
			// If uses contains only x it's fine
			if (usesAndModifies.size() == 1 && usesAndModifies.contains(varName)) {
				// Check the expression has only x and has a PLUS or MINUS operator on it
				expectX = true;
				if (analyzeExpr(varName, ((AssignStmt) node).getRhs()))
					return false;
				Expr expr = transformExpr(varName, ((AssignStmt) node).getRhs());
				containsX.add(true);
				vars.add(varName);
				exprs.add(expr);
			} else if (usesAndModifies.size() == 0) {
				containsX.add(false);
				vars.add(varName);
				exprs.add(((AssignStmt) node).getRhs());
			} else {
				return false;
			}
		}
	
		
		return result;
	}
	
	// x can only be in a BinaryExpr and only appear once, otherwise we should return false
	private boolean analyzeExpr(String varName, Expr expr) {
		if (expr instanceof BinaryExpr) {
			Expr exprLhs = ((BinaryExpr) expr).getLhs();
			Expr exprRhs = ((BinaryExpr) expr).getRhs();
			Expr checkExpr = null;
			int operator = ((BinaryExpr) expr).getOperator();
			
			// If LHS expression contains x
			if (exprLhs instanceof DeclRef && ((DeclRef) exprLhs).getName().equals(varName)) {
				checkExpr = exprLhs;
			}
			// If RHS expression contains x
			if (exprRhs instanceof DeclRef && ((DeclRef) exprRhs).getName().equals(varName)) {
				checkExpr = exprRhs;
			}
			
			if (checkExpr != null) {
				if (operator != BinaryExpr.ADD && operator != BinaryExpr.SUBTRACT)
					return false;
				if (expectX == false)
					return false;
				expectX = false;
				return analyzeExpr(varName, checkExpr);
			} else {
				analyzeExpr(varName, exprLhs);
				analyzeExpr(varName, exprRhs);
			}
		}
		if (expr instanceof IntLiteral) {
			return true;
		}
		if (expr instanceof UnaryExpr) {
			// We can't handle x to be part of unary expr
			boolean previous = expectX;
			expectX = false;
			boolean result = analyzeExpr(varName, ((UnaryExpr) expr).getOperand());
			expectX = previous;
			return result;
		}
		// This is for DeclRef that should not be reached
		return false;
	}
	
	private Expr transformExpr(String varName, Expr expr) {
		// x can only be in a BinaryExpr and can only appear once
		if (expr instanceof BinaryExpr) {
			Expr exprLhs = ((BinaryExpr) expr).getLhs();
			Expr exprRhs = ((BinaryExpr) expr).getRhs();
			
			// If LHS expression contains x
			if (exprLhs instanceof DeclRef && ((DeclRef) exprLhs).getName().equals(varName)) {
				return new BinaryExpr(((BinaryExpr) expr).getOperator(), new IntLiteral(0), exprRhs);
			}
			// If RHS expression contains x
			if (exprRhs instanceof DeclRef && ((DeclRef) exprRhs).getName().equals(varName)) {
				return new BinaryExpr(((BinaryExpr) expr).getOperator(), exprLhs, new IntLiteral(0));
			}
		}
		
		return transformExpr(varName, expr);
		
	}
}
