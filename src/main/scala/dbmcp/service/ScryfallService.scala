package dbmcp.service

import cats.effect.IO
import cats.syntax.option.* 
import dbmcp.{Card, SetId, SetNum}
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Method
import org.http4s.Request
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.*
import org.http4s.ember.client.*
import org.http4s.headers.Accept
import org.http4s.implicits.*
import org.http4s.headers.`User-Agent`
import org.http4s.ProductId

class ScryfallService(client: Client[IO]):
  val DeckBuilderProductId = ProductId("Deck Builder MCP", "v0.1.0-SNAPSHOT".some)

  def findByName(name: String, setId: Option[SetId], exact: Boolean = false): IO[Card] =
    val uri = uri"https://api.scryfall.com/cards/named" +? (if (exact) "exact" else "fuzzy", name)

    val request = Request[IO](
      method = Method.GET,
      uri = setId.fold(uri)(set => uri +? ("set", set)),
      headers = Headers(
        Accept(MediaType.application.json),
        `User-Agent`(DeckBuilderProductId)
        )
      )

    client.expect[Card](request)

  def findBySet(setId: SetId, setNum: SetNum): IO[Card] =
    val request = Request[IO](
      method = Method.GET,
      uri = uri"https://api.scryfall.com/cards" / setId / setNum,
      headers = Headers(
        Accept(MediaType.application.json),
        `User-Agent`(DeckBuilderProductId)
      )
    )

    client.expect[Card](request)
