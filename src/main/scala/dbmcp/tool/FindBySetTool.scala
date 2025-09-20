package dbmcp.tools

import cats.effect.IO
import cats.syntax.all._
import ch.linkyard.mcp.server.ToolFunction
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import dbmcp.service.ScryfallService
import dbmcp.{ SetId, SetNum }
import io.circe.generic.auto.given

object FindBySetTool extends ToolHelper:
  private case class Input(setId: SetId, setNum: SetNum)

  def apply(scryfallService: ScryfallService): ToolFunction[IO] =
    ToolFunction.text[IO, Input](
      info = ToolFunction.Info(
        name = "find_card_by_set",
        title = "Find Card By Set and Number".some,
        description =
          "Searches Scryfall for an MtG card by Set Code and Collection Number".some,
        effect = Effect.ReadOnly,
        isOpenWorld = true
      ),
      f = (fbsi, _) =>
        scryfallService
          .findBySet(fbsi.setId, fbsi.setNum)
          .map(_.show)
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
