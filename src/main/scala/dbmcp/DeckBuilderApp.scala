package dbmcp

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.kernel.Resource
import ch.linkyard.mcp.jsonrpc2.transport.StdioJsonRpcConnection
import ch.linkyard.mcp.server.McpServer

object DeckBuilderApp extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    DeckBuilderMcpServer().start(
      StdioJsonRpcConnection.create[IO],
      e => IO(System.err.println(s"Error: $e")),
    ).useForever.as(ExitCode.Success)
