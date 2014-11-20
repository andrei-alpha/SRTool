package srt.tool;

import java.io.IOException;
import java.util.ArrayList;

import srt.ast.BlockStmt;
import srt.ast.Expr;
import srt.ast.Invariant;
import srt.ast.Program;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class HoudiniVerifierVisitor extends DefaultVisitor {
	private Program program;
	private CLArgs clArgs;
	
	public HoudiniVerifierVisitor(Program program, CLArgs clArgs) {
		super(true);
		this.clArgs = clArgs;
		this.program = program;
	}

	@Override
	public Object visit(BlockStmt blockStmt) {
		// This is not a Houdini block
		
		if (blockStmt.getBaseWhileStmt() == null)
			return super.visit(blockStmt);
			
		while(true) {
			ArrayList<Expr> failedInvs;
			try {
				failedInvs = checkProgram(program);
			} catch (IOException e) {
				break;
			} catch (InterruptedException e) {
				break;
			}
			
			// Remove failing invariants
			boolean success = false;
			if (failedInvs != null) {
				for (Expr failedInv : failedInvs)
					success |= removeFailingInvariants(blockStmt.getBaseWhileStmt(), failedInv);
			}
				
			if (!success) {
				makeCandidatesTrue(blockStmt.getBaseWhileStmt());
				break;
			}
			
			blockStmt = HoudiniTransformerVisitor.transformWhileStmt(blockStmt.getBaseWhileStmt());
			// Update the blockStmt in the Program
			changeChildInParent(blockStmt);
		}
		
		return super.visit(blockStmt.getBaseWhileStmt());
	}
	
	public ArrayList<Expr> checkProgram(Program baseProgram) throws IOException, InterruptedException {
		/*if (clArgs.verbose) {
			PrinterVisitor printerVisitor = new PrinterVisitor();
			String code = printerVisitor.visit(baseProgram);
			System.out.println("Before transformation:\n" + code);
		}*/
		
		Program program = baseProgram.copy();
		program = (Program) new LoopAbstractionVisitor().visit(program);
		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);

		/*if (clArgs.verbose) {
			PrinterVisitor printerVisitor = new PrinterVisitor();
			String code = printerVisitor.visit(program);
			System.out.println("After transformation:\n" + code);
		}*/
		
		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);

		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
		builder.buildQuery();
		String smtQuery = builder.getQuery();

		// Submit query to SMT solver.
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		String queryResult = "";
		try {
			queryResult = process.execute(smtQuery, 4);
		} catch (ProcessTimeoutException e) {
			return null;
		}

		if (queryResult.startsWith("sat")) {
			return builder.getFailedHoudini(queryResult);
		}
		return null;
	}
	
	private boolean removeFailingInvariants(WhileStmt whileStmt, Expr failedExpr) {
		ArrayList<Invariant> invs = (ArrayList<Invariant>) (whileStmt.getInvariantList().getInvariants());
		if (clArgs.verbose) {
			System.out.println("Failed invariant: " + failedExpr);
		}
		
		for (int i = 0; i < invs.size(); ++i) {
			Invariant inv = invs.get(i);			
			if (inv.getExpr().equals(failedExpr)) {
				whileStmt.removeInvariantAt(i);
				return true;
			}
		}
		return false;
	}
	
	private void makeCandidatesTrue(WhileStmt whileStmt) {
		ArrayList<Invariant> invs = (ArrayList<Invariant>) (whileStmt.getInvariantList().getInvariants());
		for (int i = 0; i < invs.size(); ++i) {
			whileStmt.setCandidateAt(i, false);
		}
	}
}
