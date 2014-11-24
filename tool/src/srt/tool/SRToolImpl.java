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
		ConstantFoldingVisitor constantFoldingVisitor = new ConstantFoldingVisitor();
		DeadCodeEliminationVisitor deadCodeEliminationVisitor = new DeadCodeEliminationVisitor();
		while (true) { 
			loopOptimizerVisitor.resetModified();
			constantFoldingVisitor.resetModified();
			deadCodeEliminationVisitor.resetModified();
			program = (Program) loopOptimizerVisitor.visit(program);
			program = (Program) constantFoldingVisitor.visit(program);
			program = (Program) deadCodeEliminationVisitor.visit(program);
			
			if (!loopOptimizerVisitor.hasModified() && !deadCodeEliminationVisitor.hasModified())
				break;
		}

		// Output the program as text after being optimized (for debugging).
		if (clArgs.verbose) {
			System.out.println("\nAfter optimizations.\n");
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}
		
		// Run the normal BMC transformations to use SMT-Solver
		SMTBuilder smtBuilder = new SMTBuilder(program.copy(), clArgs, CLArgs.BMC);
		Thread smtThread = new Thread(smtBuilder);
		smtThread.run();
		solvers.add(smtThread);
		builders.add(smtBuilder);
		
		// Run in INVGEN mode and use SMT-Solver
/*		SMTBuilder smtBuilder2 = new SMTBuilder(program.copy(), clArgs, CLArgs.INVGEN);
		Thread smtThread2 = new Thread(smtBuilder2);
		smtThread2.run();
		solvers.add(smtThread2);
		builders.add(smtBuilder2);
		
		// Run program to try to find input that fails assertions
		ExecutableBuilder execBuilder = new ExecutableBuilder(program, clArgs);
		Thread execThread = new Thread(execBuilder);
		execThread.run();
		solvers.add(execThread);
		builders.add(execBuilder);*/
	}
	
	public SRToolResult go() throws InterruptedException {
		if (clArgs.mode.equals(CLArgs.COMP)) {
			competition();
		} else {
			// Run the normal transformations to use SMT-Solver
			SMTBuilder smtBuilder = new SMTBuilder(program, clArgs, clArgs.mode);
			Thread smtThread = new Thread(smtBuilder);
			smtThread.run();
			solvers.add(smtThread);
			builders.add(smtBuilder);
		}
		
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
