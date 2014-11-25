package srt.tool;

import java.util.HashMap;
import java.util.HashSet;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Stmt;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.FunctionUtil;

public class ConstantFoldingVisitor extends DefaultVisitor {
	private WhileStmt lastLoop;
	private HashMap<String, Integer> state;
	
	public ConstantFoldingVisitor() {
		super(true);
		state = new HashMap<String, Integer>();
	}

	@Override
	public Object visit(DeclRef declRef) {
		declRef = (DeclRef) super.visit(declRef);
		
		if (state.containsKey(declRef.getName())) {
			return new IntLiteral(state.get(declRef.getName()));
		}
		
		return declRef;
	}
	
	@Override
	public Object visit(AssignStmt assignStmt) {
		Expr newRhs = (Expr) super.visit(assignStmt.getRhs());
		assignStmt = new AssignStmt(assignStmt.getLhs(), newRhs);
		DeclRef lhs = assignStmt.getLhs();
		Expr rhs = assignStmt.getRhs();
		
		if (rhs instanceof IntLiteral) {
			state.put(lhs.getName(), ((IntLiteral) rhs).getValue());
		} else {
			state.remove(lhs.getName());
		}
		return assignStmt;
	}
	
	@Override
	public Object visit(AssumeStmt assumeStmt) {
		assumeStmt = (AssumeStmt) super.visit(assumeStmt);
		Expr cond = assumeStmt.getCondition();
		if (cond instanceof IntLiteral && ((IntLiteral) cond).getValue() != 0)
			return null;
		return assumeStmt;
	}
	
	@Override
	public Object visit(AssertStmt assertStmt) {
		assertStmt = (AssertStmt) super.visit(assertStmt);
		Expr cond = assertStmt.getCondition();
		if (cond instanceof IntLiteral && ((IntLiteral) cond).getValue() != 0)
			return null;
		return assertStmt;
	}

	@Override
	public Object visit(BinaryExpr binaryExpr) {
		binaryExpr = (BinaryExpr) super.visit(binaryExpr);
		int operator = binaryExpr.getOperator();
		Expr lhs = binaryExpr.getLhs();
		Expr rhs = binaryExpr.getRhs();
		
		if (lhs instanceof IntLiteral && rhs instanceof IntLiteral)
			return new IntLiteral(binaryExpr.calculate());
			
		// Eliminate simple division cases
		if (operator == BinaryExpr.DIVIDE) {
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 1)
				return lhs;
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return new IntLiteral(0);
		}
		// Eliminate simple add cases
		if (operator == BinaryExpr.ADD) {
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return rhs;
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 0)
				return lhs;
		}
		// Eliminate simple subtract cases
		if (operator == BinaryExpr.SUBTRACT) {
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return new UnaryExpr(UnaryExpr.UMINUS, rhs);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 0)
				return lhs;
		}
		// Eliminate simple multiply cases
		if (operator == BinaryExpr.MULTIPLY) {
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return new IntLiteral(0);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 0)
				return new IntLiteral(0);
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 1)
				return rhs;
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 1)
				return lhs;
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == -1)
				return new UnaryExpr(UnaryExpr.UMINUS, rhs);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == -1)
				return new UnaryExpr(UnaryExpr.UMINUS, lhs);;
		}
		// Eliminate simple and cases
		if (operator == BinaryExpr.LAND) {
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return new IntLiteral(0);
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 1)
				return new IntLiteral(1);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 0)
				return new IntLiteral(0);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 1)
				return new IntLiteral(1);
		}
		// Eliminate simple or cases
		if (operator == BinaryExpr.LOR) {
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 0)
				return rhs;
			if (lhs instanceof IntLiteral && ((IntLiteral) lhs).getValue() == 1)
				return new IntLiteral(1);
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 0)
				return lhs;
			if (rhs instanceof IntLiteral && ((IntLiteral) rhs).getValue() == 1)
				return new IntLiteral(1);
		}
		
		return binaryExpr;
	}
	
	@Override
	public Object visit(UnaryExpr unaryExpr) {
		unaryExpr = (UnaryExpr) super.visit(unaryExpr);
		if (unaryExpr.getOperand() instanceof IntLiteral) {
			return new IntLiteral(unaryExpr.calculate());
		}
		return unaryExpr;
	}
	
	@Override
	public Object visit(WhileStmt whileStmt) {
		WhileStmt prevLoop = lastLoop;
		lastLoop = whileStmt;
		
		HashSet<String> modifies = whileStmt.getModifies();
		// Remove all the variables from the whileStmt modset
		for (String var : modifies)
			if (state.containsKey(var))
				state.remove(var);
		
		whileStmt = (WhileStmt) super.visit(whileStmt);
		
		// Remove all the variables from the whileStmt
		for (String var : modifies)
			if (state.containsKey(var))
				state.remove(var);
		
		lastLoop = prevLoop;
		return whileStmt;
	}
	
	@Override
	public Object visit(IfStmt ifStmt) {
		HashMap<String, Integer> ifState, elseState;
		ifState = new HashMap<String, Integer>();
		elseState = new HashMap<String, Integer>();
		
		state = ifState;
		Expr ifCond = (Expr) super.visit(ifStmt.getCondition());
		Stmt thenStmt = (Stmt) super.visit(ifStmt.getThenStmt());

		state = elseState;
		Stmt elseStmt = (Stmt) super.visit(ifStmt.getElseStmt());
		elseState = state;
		
		state = FunctionUtil.getIntersection(ifState, elseState);
		// If we haven't modified the AST inside the ifStmt return the original ifStmt 
		if (!hasModified())
			return ifStmt;
		return new IfStmt(ifCond, thenStmt, elseStmt);
	}
}
