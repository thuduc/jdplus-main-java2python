# Proof-of-concept: Java to Python conversion using Claude Code
JDemetra+ (Java tool for seasonal adjustment and time series analysis) source repo is located at https://github.com/jdemetra/jdplus-main

This POC is to evaluate Claude Code (an agentic coding tool from Anthropic: https://www.anthropic.com/claude-code) for its ability to convert a toolkit written in Java to Python.

#### Conversion Process: 
* Step 1 - use a reasoning LLM that's able to analyze an existing code repository, then put together a comprehensive migration plan for converting the entire project's codebase from Java to Python. We used Anthropic's Claude Opus 4 LLM for our reasoning LLM. We chose Opus 4 over OpenAI's ChatGPT o3 (advanded reasoning) and Google Gemini 2.5 Pro (reasoning) due to its advanced ability to analyze code. 
* Step 2 - use this migration plan (see migration_plan.md) with Claude Code (together with Claude Opus 4 LLM, known as the most advanded model for agentic coding tasks) to implement all tasks in all phases defined in the migration plan. The migration plan includes requirements for comprehensive test coverage.

The conversion took Claude Code about 8 hours to complete. This includes the successful passing of all unit and integration tests. See jdemetra_py/TEST_SUMMARY.md for details. The converted python codebase resides under jdemetra_py folder.


## Running the code
See jdemetra_py/README.md

## All prompts issued to Claude Code
The complete list of prompts issued to Clause Code is listed below:

> think hard and create a plan to convert the existing codebase from Java to Python. Save this plan under migration_plan.md

> Go ahead and implement @migration_plan.md 

> Run all tests and make sure there are no failures. Save to test results to TEST_SUMMARY.md
