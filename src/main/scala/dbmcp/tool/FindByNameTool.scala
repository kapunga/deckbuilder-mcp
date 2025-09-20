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
import dbmcp.SetId
import dbmcp.service.ScryfallService
import io.circe.generic.auto.given

object FindByNameTool extends ToolHelper:
  private case class Input(
      name: String,
      setId: Option[SetId],
      exact: Boolean = false
  )

  def apply(scryfallService: ScryfallService): ToolFunction[IO] =
    ToolFunction.text[IO, Input](
      info = ToolFunction.Info(
        name = "find_card_by_name",
        title = "Find Card By Name".some,
        description =
          "Searches Scryfall for an MtG card by name. Set code for narrowing down results is optional. If exact match is set, will look for an exact match, otherwise the search will be fuzzy.".some,
        effect = Effect.ReadOnly,
        isOpenWorld = true
      ),
      f = (fbni, _) =>
        scryfallService
          .findByName(fbni.name, fbni.setId, fbni.exact)
          .map(_.show)
          .handleErrorWith(ex => IO.raiseError(toMcpError(ex)))
    )
