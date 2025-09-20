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
