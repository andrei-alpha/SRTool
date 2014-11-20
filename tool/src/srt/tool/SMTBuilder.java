package srt.tool;

import java.io.IOException;

import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.SRTool.SRToolResult;
import srt.tool.exception.ProcessTimeoutException;

public class SMTBuilder extends Builder {
	private SMTLIBQueryBuilder smtlibQueryBuilder;
	
	public SMTBuilder(Program program, CLArgs clArgs) {
		super(program, clArgs);
	}

	@Override
	public void run() {
		try {
			String smtQuery = transformProgram();
			runResult = runQuery(smtQuery);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String transformProgram() {
		// Transform program using visitors here
		if (clArgs.mode.equals(CLArgs.BMC) || clArgs.mode.equals(CLArgs.COMP)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else {
			if (clArgs.mode.equals(CLArgs.INVGEN))
				program = (Program) new InvariantGenVisitor().visit(program);
			if (clArgs.mode.equals(CLArgs.INVGEN) || clArgs.mode.equals(CLArgs.HOUDINI)) {
				program = (Program) new HoudiniTransformerVisitor(program).visit(program);
				program = (Program) new HoudiniVerifierVisitor(program, clArgs).visit(program);
			}
			program = (Program) new LoopAbstractionVisitor().visit(program);
		}
		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);
		
		// Output the program as text after being transformed (for debugging).
		if (clArgs.verbose) {
			System.out.println("\nAfter transformations.\n");
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}

		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);

		// Convert constraints to SMTLIB String.
		smtlibQueryBuilder = new SMTLIBQueryBuilder(ccv);
		smtlibQueryBuilder.buildQuery();

		String smtQuery = smtlibQueryBuilder.getQuery();

		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}
		return smtQuery;
	}
	
	public SRToolResult runQuery(String smtQuery) throws IOException, InterruptedException {
		// Submit query to SMT solver.
		// You can use other solvers.
		//ProcessExec process = new ProcessExec("cvc4", "--lang", "smt2");
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		String queryResult = "";
		try {
			queryResult = process.execute(smtQuery, clArgs.timeout);
		} catch (ProcessTimeoutException e) {
			if (clArgs.verbose) {
				System.out.println("Timeout!");
			}
			return SRToolResult.UNKNOWN;
		}
		
		if (queryResult.startsWith("unsat")) {
			return SRToolResult.CORRECT;
		}
		
		if (queryResult.startsWith("sat")) {
			if (clArgs.mode.equals(CLArgs.BMC) && smtlibQueryBuilder.isUnwindingFailure(queryResult))
				return SRToolResult.INCORRECT;
			else if (!smtlibQueryBuilder.isUnwindingFailure(queryResult))
				return SRToolResult.INCORRECT;
		}
		return SRToolResult.UNKNOWN;
	}
}
