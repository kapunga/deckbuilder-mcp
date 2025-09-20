package dbmcp

import cats.effect.{ IO, Resource }
import cats.syntax.all._
import ch.linkyard.mcp.protocol.Initialize.PartyInfo
import ch.linkyard.mcp.server.McpServer.{ Client, ConnectionInfo }
import ch.linkyard.mcp.server.{ McpServer, ToolFunction }
import dbmcp.service.ScryfallService
import dbmcp.tools._
import org.http4s.ember.client.EmberClientBuilder

class DeckBuilderMcpServer extends McpServer[IO]:
  override def initialize(
      client: Client[IO],
      info: ConnectionInfo[IO]
  ): Resource[IO, McpServer.Session[IO]] =
    for {
      httpClient <- EmberClientBuilder.default[IO].build
    } yield DeckBuilderSession(ScryfallService(httpClient))

private class DeckBuilderSession(
    scryfallService: ScryfallService
) extends McpServer.Session[IO]
    with McpServer.ToolProvider[IO]:

  override val serverInfo: PartyInfo = PartyInfo(
    name = "Deck Builder MCP",
    version = "v0.1.0-SNAPSHOT"
  )

  override def instructions: IO[Option[String]] = None.pure

  override val tools: IO[List[ToolFunction[IO]]] =
    List(
      CardSearchTool(scryfallService),
      FindByIdTool(scryfallService),
      FindByNameTool(scryfallService),
      FindBySetTool(scryfallService)
    ).pure
