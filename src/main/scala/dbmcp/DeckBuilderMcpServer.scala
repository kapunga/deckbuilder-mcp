package dbmcp

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import ch.linkyard.mcp.protocol.Initialize.PartyInfo
import ch.linkyard.mcp.server.McpServer
import ch.linkyard.mcp.server.McpServer.Client
import ch.linkyard.mcp.server.McpServer.ConnectionInfo
import ch.linkyard.mcp.server.ToolFunction
import org.http4s.client.Client as HttpClient
import org.http4s.ember.client.EmberClientBuilder
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import io.circe.generic.auto.given

class DeckBuilderMcpServer extends McpServer[IO]:
  override def initialize(client: Client[IO], info: ConnectionInfo[IO]): Resource[IO, McpServer.Session[IO]] =
    for {
      httpClient <- EmberClientBuilder.default[IO].build
    } yield DeckBuilderSession(ScryfallService(httpClient))

private class DeckBuilderSession(
  scryfallService: ScryfallService
) extends McpServer.Session[IO] with McpServer.ToolProvider[IO]:

  override val serverInfo: PartyInfo = PartyInfo(
    name = "Deck Builder MCP",
    version = "v0.1.0-SNAPSHOT"
  )

  override def instructions: IO[Option[String]] = None.pure

  override val tools: IO[List[ToolFunction[IO]]] = List(findBySetTool(scryfallService)).pure

private case class FindBySetInput(setId: SetId, setNum: SetNum)

private def findBySetTool(scryfallService: ScryfallService): ToolFunction[IO] =
  ToolFunction.text[IO, FindBySetInput](
    info = ToolFunction.Info(
      name = "find_card_by_set",
      title = "Find Card By Set and Number".some,
      description = "Searches Scryfall for an MtG card by Set Code and Collection Number".some,
      effect = Effect.ReadOnly,
      isOpenWorld = true    
    ),
    f = (fbsi, _) => scryfallService.findBySet(fbsi.setId, fbsi.setNum).map(_.show)
  )
