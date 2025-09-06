# Deck Builder MCP

An MCP server aimed at making deck building with a foundation model delightful.

**THIS IS VERY ALPHA, EXPECT RAPID FEATURE CHANGE**

## Current tools
- `find_card_by_set` - Searches Scryfall for an MtG card by Set Code and Collection Number

## Use with Claude
To use it, check out the repo, run `sbt assembly` to build the project.

**For Claude Code**
```bash
$ claude mcp add deckbuilder-mcp -- /path/to/repo/run-mcp-server.sh
```

**For Claude Desktop**
```json
"mcpServers": {
    "deckbuilder-mcp": {
        "type": "stdio",
        "command": "/path/to/repo/deckbuilder-mcp/run-mcp-server.sh"
    }
}
```
