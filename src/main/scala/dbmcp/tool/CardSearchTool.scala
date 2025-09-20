package dbmcp.tools

import cats.effect.IO
import cats.syntax.all.*
import ch.linkyard.mcp.server.ToolFunction
import dbmcp.SetId
import dbmcp.service.ScryfallService
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import io.circe.generic.auto.given

object CardSearchTool extends ToolHelper:
  private case class Input(searchTerm: String, limit: Int = 20)

  def apply(scryfallService: ScryfallService): ToolFunction[IO] =
    ToolFunction.text[IO, Input](
      info = ToolFunction.Info(
        name = "scryfall_card_search",
        title = "Search for Cards on Scryfall".some,
        description =
          """Search for MTG cards using Scryfall syntax (e.g., 't:creature c:black cmc<=3',
            |'o:draw', 'f:standard'). Returns cards in efficient markdown format with
            |essential game text. Supports all Scryfall search operators and complex queries. 
            |""".stripMargin.some,
        effect = Effect.ReadOnly,
        isOpenWorld = true
      ),
      f = (csi, _) =>
        scryfallService.search(csi.searchTerm, csi.limit)
          .map(_.map(_.show).mkString("\n\n=====\n\n"))
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
