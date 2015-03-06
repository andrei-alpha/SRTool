# SRTool

In this project we implemented a *SRTool*, to analyse with the aim of detecting bugs in single-procedure *Simple C* programs.

### Overview

We applied standard and precise Software Reliability techniques for non-loop code like *Predication*. It is common knolwedge that for analyzing loops there isn't a perfect approach so we used both under-aproximation techniques like *BMC* and over-aproximation techniques like *Loop Summarisation* combined with *Invariant Generation* and *Houdini*..
In the end we participated in a competition between similar tools. For that we applied different heuristics to enrich the default techniques together with compiler optimizations to reduce the workload of the SMT solver where possible. The pinnacle of tool's reliability came when combining techniques so they complement their weaknesses. 

### Running the tool

Before you can build the tool you need to create an enviroment variable *TOOL_DIR* that points to the root directory of the project. Next you need to install project dependencies and add the binaries to *PATH*.

1. [Z3 SMT Solver](http://z3.codeplex.com/)
2. [CVC4 Theorem Prover](https://github.com/CVC4/CVC4)

After that you can build the tool and run it using *srt_build* and *srt_tun* script from the *tool* directory. Additionaly you can use the scripts found in the *scripts* directory that will run the tool on a predefined set of tests like *Invariant Generation*, *Houdini* or *Competition*.

### Future work

This is an experimental project done in limited time so there are still some bugs. We hope to resolve them in the future.
