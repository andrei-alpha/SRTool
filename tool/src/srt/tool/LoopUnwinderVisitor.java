package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.EmptyStmt;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopUnwinderVisitor extends DefaultVisitor {

	private boolean unsound;
	private int outerLoops = 0;
	private int defaultUnwindBound;

	public LoopUnwinderVisitor(boolean unsound,
			int defaultUnwindBound) {
		super(true);
		
		this.unsound = unsound;
		this.defaultUnwindBound = defaultUnwindBound;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		// Go recursively to remove any infinite loops
		outerLoops += 1;
		whileStmt = (WhileStmt) super.visit(whileStmt);
		outerLoops -= 1;
		
		// Smart unwinding, we know how many outer and inner loops
		int totalLoops = outerLoops + whileStmt.getLoopCount();
		int unwindBound = greedyUnwind(totalLoops, outerLoops);
		
		// If we detected an infinite loop
		HashSet<String> modifies = whileStmt.getBody().getModifies();
		HashSet<String> uses = whileStmt.getCondition().getUses();
		
		boolean infiniteLoop = true;
		for (String var : uses) {
			if (modifies.contains(var)) {
				infiniteLoop = false;
				break;
			}
		}
		if (infiniteLoop && !whileStmt.hasAsserts()) {
			AssumeStmt thenStmt = new AssumeStmt(new IntLiteral(0));
			EmptyStmt elseStmt = new EmptyStmt();
			IfStmt ifStmt = new IfStmt(whileStmt.getCondition(), thenStmt, elseStmt);
			return ifStmt;
		}
		
		if (whileStmt.getBound() != null)
			unwindBound = whileStmt.getBound().getValue();
		
		outerLoops += 1;
		Stmt stmt = (Stmt) super.visit(unwindLoop(whileStmt, unwindBound, 0));
		outerLoops -= 1;
		return stmt;
	}
	
	private int greedyUnwind(int totalLoops, int outerLoops) {
		if (totalLoops > 4)
			return 2;
		// 3 * 3 * 3 * 3 instructions
		if (totalLoops == 4)
			return (outerLoops == 0) ? 3 : 2;
		// 5 * 4 * 4 instructions
		if (totalLoops == 3)
			return (outerLoops == 0) ? 4 : 3;
		// 9 * 9 instructions
		if (totalLoops == 2)
			return 8;
		return defaultUnwindBound;
	}

	private Stmt unwindLoop(WhileStmt whileStmt, int iterations, int level) {
		if (iterations < 0) {
			// assume false for last unwind block
			AssumeStmt assumeStmt = new AssumeStmt(new IntLiteral(0));
			if (unsound == false) {
				AssertStmt assertStmt = new AssertStmt(new IntLiteral(0));
				assertStmt.makeUnwinding();
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
		
		if (level == 0) {
			Expr notCondition = new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition());
			stmts.add(new AssumeStmt(notCondition));
		}
			
		return new BlockStmt(stmts, whileStmt.getNodeInfo());
	}

}
