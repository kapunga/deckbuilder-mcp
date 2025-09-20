package dbmcp.tools

import ch.linkyard.mcp.jsonrpc2.JsonRpc.ErrorCode
import ch.linkyard.mcp.server.McpError
import ch.linkyard.mcp.server.McpError.McpErrorException
import dbmcp.service.ScryfallError

trait ToolHelper:
  def toMcpError(t: Throwable): McpErrorException = t match {
    case ScryfallError.NotFound(details) =>
      McpError.error(ErrorCodes.NotFound, details)
    case ScryfallError.Ambiguous(details) =>
      McpError.error(ErrorCodes.AmbiguousSearch, details)
    case ScryfallError.Generic(code, details) =>
      McpError.error(ErrorCodes.Generic, details)
    case t =>
      McpError.error(
        ErrorCodes.Throwable,
        s"Internal Error: ${t.getClass().getCanonicalName()} - ${t.getMessage()}"
      )
  }

object ErrorCodes:
  // Error code definitions:
  lazy val Generic = ErrorCode.Other(-1000)
  lazy val AmbiguousSearch = ErrorCode.Other(-1001)
  lazy val NotFound = ErrorCode.Other(-1002)
  lazy val Throwable = ErrorCode.Other(-1999)
