package dbmcp.tools

import cats.effect.IO
import cats.syntax.all.*
import ch.linkyard.mcp.server.ToolFunction
import dbmcp.SetId
import dbmcp.service.ScryfallService
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import io.circe.generic.auto.given

object FindByNameTool extends ToolHelper:
  private case class Input(name: String, setId: Option[SetId], exact: Boolean = false)

  def apply(scryfallService: ScryfallService): ToolFunction[IO] =
    ToolFunction.text[IO, Input](
      info = ToolFunction.Info(
        name = "find_card_by_name",
        title = "Find Card By Name".some,
        description = "Searches Scryfall for an MtG card by name. Set code for narrowing down results is optional. If exact match is set, will look for an exact match, otherwise the search will be fuzzy.".some,
        effect = Effect.ReadOnly,
        isOpenWorld = true
      ),
      f = (fbni, _) => 
        scryfallService.findByName(fbni.name, fbni.setId, fbni.exact)
          .map(_.show)
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
