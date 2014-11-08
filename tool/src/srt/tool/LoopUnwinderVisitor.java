package srt.tool;

import java.util.ArrayList;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.EmptyStmt;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopUnwinderVisitor extends DefaultVisitor {

	private boolean unsound;
	private int defaultUnwindBound;

	public LoopUnwinderVisitor(boolean unsound,
			int defaultUnwindBound) {
		super(true);
		this.unsound = unsound;
		this.defaultUnwindBound = defaultUnwindBound;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		int unwindBound = defaultUnwindBound;
		if (whileStmt.getBound() != null)
			unwindBound = whileStmt.getBound().getValue();
		
		return super.visit(unwindLoop(whileStmt, unwindBound, 0));
	}
	
	private Stmt unwindLoop(WhileStmt whileStmt, int iterations, int level) {
		if (iterations < 0) {
			// assume false for last unwind block
			AssumeStmt assumeStmt = new AssumeStmt(new IntLiteral(0));
			if (unsound == false) {
				AssertStmt assertStmt = new AssertStmt(new IntLiteral(0));
				return new BlockStmt(new Stmt[] { assertStmt, assumeStmt },
						whileStmt.getNodeInfo());
			}
			return assumeStmt;
		}
		
		ArrayList<Stmt> stmts = new ArrayList<Stmt>(); 
		if (level != 0)
			stmts.add(whileStmt.getBody());
		
		// Add all loop invariants
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			stmts.add(new AssertStmt(inv.getExpr()));
		}
		
		// Loop conditional that contains next unwind block
		Expr condition = whileStmt.getCondition();
		Stmt thenStmt = unwindLoop(whileStmt, iterations - 1, level + 1);
		Stmt elseStmt = new EmptyStmt();
		IfStmt ifStmt = new IfStmt(condition, thenStmt, elseStmt);
		stmts.add(ifStmt);
		
		return new BlockStmt(stmts, whileStmt.getNodeInfo());
	}

}
