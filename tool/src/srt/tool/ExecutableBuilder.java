package srt.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.SRTool.SRToolResult;
import srt.tool.exception.ProcessTimeoutException;

public class ExecutableBuilder extends Builder {
	
	public ExecutableBuilder(Program program, CLArgs clArgs) {
		super(program, clArgs);
	}

	@Override
	public void run() {
		try {
			String code = transformProgram();
			runResult = runProgram(code);
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
	}
	
	public String transformProgram() {
		String code = "";
		// Get a list of free vars
		ArrayList<String> freeVars = getFreeVariables();
		String params = "", args = "";
		boolean isFirst = true;
		for (String var : freeVars) {
			if (var.contains("$"))
				params += (isFirst ? "" : ", ") + "int " + var.replaceAll("\\$", "spc_var_");
			else
				params += (isFirst ? "" : ", ") + "int " + var;
			args += (isFirst ? "" : ", ") + "newValue()";
			isFirst = false;
		}
		
		// Generate code for assert and havoc
		code += "#include <cstdio>\n#include <cstdlib>\n#include <ctime>\n\nlong ";
		code += "base = 10;\n\nint newValue() {\n@return (rand() - rand()) % base;\n}";
		code += "\n\nvoid assert(bool v) {\n@if (!v) {\n@@printf(@2incorrect@2);\n@@ex";
		code += "it(-1);\n@}\n}\n\nvoid havoc(int &x) {\n@x = newValue();\n}\n\n";
		
		// Parse the file into a string
		String programText = new PrinterVisitor().visit(program);
		code += programText;
		
		// Remove var declaration, assumes, inv, cand and change main to start
		code = code.replaceAll("\\$", "spc_var_");
		code = code.replaceFirst("[a-z]*.?main\\(.*\\)", "void start(" + params + ")");
		code = code.replaceAll(".*inv\\(.*\\).*\n", "");
		code = code.replaceAll(".*cand\\(.*\\).*\n", "");
		code = code.replaceAll("assume\\((.*)\\);", "if (\\!($1)) return;");
		code = code.replaceAll(".*int\\s.*;.*\n", "");
				
		code += "\n\nint main() {\n@for(;;) {\n@@for (int i = 0; i < 10; ++i) {\n";
		code += "@@@start(" + args + ");\n@@}\n@@base=(base<99999999?base*10:base);";
		code += "\n@}\n@return 0;\n}\n";
		code = code.replaceAll("@2", "\"");
		code = code.replaceAll("@", "  ");
		
		// For debugging purposes
		if (clArgs.verbose) {
			System.out.println(code);
		}
		
		return code;
	}
	
	public SRToolResult runProgram(String code) throws IOException, InterruptedException {
		try {
			PrintWriter out = new PrintWriter("sr-test.cpp");
			out.println(code);
			out.close();
		} catch (FileNotFoundException e) {
			return SRToolResult.UNKNOWN;
		}
		
		ProcessExec process = new ProcessExec("g++", "-o", "sr-test", "sr-test.cpp");
		try {
			process.execute("", clArgs.timeout);
		} catch(ProcessTimeoutException e) {
			return SRToolResult.UNKNOWN;
		}
		
		String runResult = "";
		process = new ProcessExec("./sr-test");
		try {
			runResult = process.execute("", clArgs.timeout);
		} catch(ProcessTimeoutException e) {
			return SRToolResult.UNKNOWN;
		}

		System.out.println("Run:" + runResult);
		if (runResult.startsWith("incorrect"))
			return SRToolResult.INCORRECT;
		return SRToolResult.MAYBE_CORRECT;
	}

	private ArrayList<String> getFreeVariables() {
		//ArrayList<String> freeVars = new ArrayList<String>();
		HashMap<String, Boolean> freeVars = new HashMap<String, Boolean>();
		
		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);
		
		for (String var : ccv.variableNames) {
			freeVars.put(var, true);
		}
		
		return new ArrayList<String>(freeVars.keySet());
	}
}
