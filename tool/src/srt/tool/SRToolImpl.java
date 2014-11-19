package srt.tool;

import java.io.IOException;

import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;

	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
	}
	
	public SRToolResult go() throws IOException, InterruptedException {
		// Experiment to generate executable 
		ExecutableBuilder execBuilder = new ExecutableBuilder(program, clArgs);
		Thread execThread = new Thread(execBuilder);
		//execThread.run();
		
		// Transform program using Visitors here.
		if (clArgs.mode.equals(CLArgs.COMP)) {
			clArgs.unwindDepth = 32;
			
			// Perform smart loops optimizations and constant folding
			LoopOptimizerVisitor loopOptimizerVisitor = new LoopOptimizerVisitor();
			while (true) { 
				loopOptimizerVisitor.resetSuccess();
				program = (Program) loopOptimizerVisitor.visit(program);
				program = (Program) new ConstantFoldingVisitor().visit(program);
				program = (Program) new DeadCodeEliminationVisitor().visit(program);
				
				if (!loopOptimizerVisitor.success)
					break;
			}
			
			// Output the program as text after being optimized (for debugging).
			if (clArgs.verbose) {
				System.out.println("\nAfter optimization.\n");
				String programText = new PrinterVisitor().visit(program);
				System.out.println(programText);
			}
		}
		
		if (clArgs.mode.equals(CLArgs.BMC) || clArgs.mode.equals(CLArgs.COMP)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else {
			if (clArgs.mode.equals(CLArgs.INVGEN))
				;// TO DO: INVGEN
			if (clArgs.mode.equals(CLArgs.INVGEN) || clArgs.mode.equals(CLArgs.HOUDINI)) {
				program = (Program) new HoudiniTransformerVisitor(program).visit(program);
				program = (Program) new HoudiniVerifierVisitor(program).visit(program);
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

		// TODO: Convert constraints to SMTLIB String.
		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
		builder.buildQuery();

		String smtQuery = builder.getQuery();

		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}

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

		// output query result for debugging
		if (clArgs.verbose) {
			// Ignore get value error if assignment is sat 
			queryResult = queryResult.replaceFirst("(.*model is not available.*)\n", "");
			System.out.println(queryResult);
		}

		if (queryResult.startsWith("unsat")) {
			return SRToolResult.CORRECT;
		}
		
		if (queryResult.startsWith("sat") && !builder.isUnwindingFailure(queryResult)) {
			return SRToolResult.INCORRECT;
		}
		
		// wait for the other thread to finish
		execThread.join();
		String runResult = execBuilder.getResult();
		
		if (runResult.startsWith("incorrect")) {
			return SRToolResult.INCORRECT;
		}
		
		// query result started with something other than "sat" or "unsat"
		return SRToolResult.UNKNOWN;
	}
}
