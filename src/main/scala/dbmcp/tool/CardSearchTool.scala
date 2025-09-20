/*
 * Copyright (c) 2025 Paul (Thor) Thordarson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dbmcp.tools

import cats.effect.IO
import cats.syntax.all._
import ch.linkyard.mcp.server.ToolFunction
import ch.linkyard.mcp.server.ToolFunction.Effect
import com.melvinlow.json.schema.generic.auto.given
import dbmcp.service.ScryfallService
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
        scryfallService
          .search(csi.searchTerm, csi.limit)
          .map(_.map(_.show).mkString("\n\n=====\n\n"))
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
