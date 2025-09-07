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
import org.http4s.Uri
import io.circe.Decoder

class ScryfallService(client: Client[IO]):
  val DeckBuilderProductId = ProductId("Deck Builder MCP", "v0.1.0-SNAPSHOT".some)

  def findByName(name: String, setId: Option[SetId], exact: Boolean): IO[Card] =
    val uri = ScryfallUris.findByName(name, setId, exact)

    callScryfall(uri)

  def findBySet(setId: SetId, setNum: SetNum): IO[Card] =
    val uri = ScryfallUris.findBySet(setId, setNum)

    callScryfall(uri)

  protected def callScryfall[A: Decoder](uri: Uri): IO[A] =
    val req = scryFallRequest(uri)

    client.expect[A](req)

  protected def scryFallRequest(uri: Uri): Request[IO] =
    Request[IO](
      method = Method.GET,
      uri = uri,
      headers = Headers(
        Accept(MediaType.application.json),
        `User-Agent`(DeckBuilderProductId)
      )
    )
