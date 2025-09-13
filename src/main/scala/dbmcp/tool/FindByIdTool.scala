package dbmcp.tools

import cats.effect.IO
import cats.syntax.all.*
import ch.linkyard.mcp.server.ToolFunction
import dbmcp.SetId
import dbmcp.service.ScryfallService
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import io.circe.generic.auto.given

object FindByIdTool extends ToolHelper:
  private case class Input(scryfallId: String)

  def apply(scryfallService: ScryfallService): ToolFunction[IO] =
    ToolFunction.text[IO, Input](
      info = ToolFunction.Info(
        name = "find_card_by_scryfall_id",
        title = "Find Card By Scryfall Id".some,
        description = "Look up a card by it's Scryfall Id.".some,
        effect = Effect.ReadOnly,
        isOpenWorld = true
      ),
      f = (fbii, _) =>
        scryfallService.findById(fbii.scryfallId)
          .map(_.show)
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
