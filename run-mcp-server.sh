#!/bin/bash
# echo "Starting deckbuilder MCP server..." >&2
# echo "Working directory: $(pwd)" >&2
# echo "Java version: $(java -version 2>&1 | head -1)" >&2

cd $HOME/code/llm-stuff/deckbuilder-mcp
exec java -jar target/scala-3.7.1/deckbuilder-mcp-assembly-0.1.0-SNAPSHOT.jar
