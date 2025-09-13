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
  private case class Input(searchTerm: String)

  def apply(scryfallService: ScryfallService): ToolFunction[IO] = ???
