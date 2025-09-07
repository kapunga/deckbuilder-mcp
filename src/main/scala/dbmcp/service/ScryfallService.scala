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
import io.circe.HCursor
import org.http4s.Response
import org.http4s.Status

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

    client.expectOr[A](req)(processError)

  protected def scryFallRequest(uri: Uri): Request[IO] =
    Request[IO](
      method = Method.GET,
      uri = uri,
      headers = Headers(
        Accept(MediaType.application.json),
        `User-Agent`(DeckBuilderProductId)
      )
    )

  protected def processError(response: Response[IO]): IO[Throwable] = 
    response.as[ScryfallApiError].map(err => {
      if (err.status == Status.NotFound) {
        if (err.`type`.contains("ambiguous")) ScryfallError.Ambiguous(err.details)
        else ScryfallError.NotFound(err.details)
      } else {
        ScryfallError.Generic(err.status.code, err.details)
      }
    })

protected case class ScryfallApiError(
  `object`: String,
  code: String,
  `type`: Option[String],
  status: Status,
  details: String)

protected object ScryfallApiError:
  given Decoder[ScryfallApiError] = (hc: HCursor) =>
    for {
      obj <- hc.downField("object").as[String]
      code <- hc.downField("code").as[String]
      t <- hc.downField("type").as[Option[String]]
      status <- hc.downField("status").as[Int].map(Status(_))
      details <- hc.downField("details").as[String]
    } yield ScryfallApiError(obj, code, t, status, details)
