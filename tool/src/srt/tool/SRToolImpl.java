package srt.tool;

import java.util.ArrayList;

import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;
	private ArrayList<Thread> solvers;
	private ArrayList<Builder> builders;
	
	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
		solvers = new ArrayList<Thread>();
		builders = new ArrayList<Builder>();
	}
	
	public void competition() {
		// More unwinding depth
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
		
		// Run program to try to find input that fails assertions
		ExecutableBuilder execBuilder = new ExecutableBuilder(program, clArgs);
		Thread execThread = new Thread(execBuilder);
		execThread.run();
		solvers.add(execThread);
		builders.add(execBuilder);
	}
	
	public SRToolResult go() throws InterruptedException {
		if (clArgs.mode.equals(CLArgs.COMP)) {
			competition();
		}
		// Run the normal BMC transformations to use SMT-Solver
		SMTBuilder smtBuilder = new SMTBuilder(program, clArgs);
		Thread smtThread = new Thread(smtBuilder);
		smtThread.run();
		solvers.add(smtThread);
		builders.add(smtBuilder);
		
		for (Thread solver : solvers)
			solver.join();
		for (Builder builder : builders) {
			SRToolResult runResult = builder.getResult();
			if (runResult == SRToolResult.CORRECT)
				return SRToolResult.CORRECT;
			if (runResult == SRToolResult.INCORRECT)
				return SRToolResult.INCORRECT;
		}
		return SRToolResult.UNKNOWN;
	}
}
