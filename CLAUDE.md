# Deck Builder MCP - Claude Context

## Project Overview
Scala-based MCP server for Magic: The Gathering deck building using a functional programming stack. The project aims to make deck building with foundation models delightful through persistent card ownership tracking and intelligent suggestions.

**Current Status:** Alpha - rapid feature development in progress

## Technology Stack
- **Core:** Scala 3.7.1 with Cats Effect 3.6.1
- **HTTP:** Http4s 0.23.30 with Ember server/client
- **JSON:** Circe 0.14.14
- **Database:** Doobie 1.0.0-RC4 + SQLite (newly added)
- **MCP:** ch.linkyard.mcp 0.3.2

## Critical Build Commands

### Compilation & Testing
- `sbt compile` - compile the project
- `sbt assembly` - build JAR for distribution
- `sbt test` - run test suite

### **MANDATORY: Always run after making changes**
```bash
sbt formatAll
```
This runs scalafmt, scalafix, and header creation. **NEVER complete a development chunk without running this.**

## Development Workflow

### Collaboration Style
- **Claude:** Handles infrastructure setup, research for unfamiliar patterns, build configuration
- **User:** Implements core Scala business logic, functional programming patterns, MCP integration
- **Approach:** ADHD-friendly small chunks with demonstrable results
- **Focus:** Claude researches and sets up things user would need to look up, user handles familiar code

### Database Architecture
- **Location:** `~/.deckbuilder-mcp/cards.db` (auto-created)
- **Schema:** `owned_cards` table with (set_id, set_number, owned_count, interested)
- **Access:** Functional approach using Doobie Resource management

### Current Structure
```
src/main/scala/dbmcp/
├── DeckBuilderApp.scala           # Main application entry point
├── database/
│   ├── Database.scala             # Connection management & initialization
│   ├── OwnedCard.scala            # Case class model
│   └── CardRepository.scala       # CRUD operations
└── [existing MCP tools]
```

## MCP Tools

### Current Tools
- `find_card_by_set` - Search by set code and collector number
- `find_card_by_name` - Search by card name

### Planned Tools (Next Phase)
- `add_owned_card` - Mark cards as owned with quantity
- `update_card_interest` - Toggle interested flag for deck building
- `list_owned_cards` - Query owned cards with filters
- `get_card_ownership` - Check ownership status

## Future Architecture
- **CLI Tool:** Current JAR as command-line utility
- **GUI App:** Electron application for casual users
- **Shared Database:** Both components access same SQLite database
- **Distribution:** Unified macOS app bundle with both CLI and GUI

## Development Notes
- Focus on functional programming patterns with IO and Resource
- Database operations use Doobie's ConnectionIO and transact pattern
- Prefer immutable data structures and pure functions
- User enjoys implementing Scala logic - let them handle the fun parts!

## Key Files to Remember
- `build.sbt` - Dependencies and build configuration
- `run-mcp-server.sh` - Development server launcher
- `Database.scala` - Contains `initializeDb` for first-time setup