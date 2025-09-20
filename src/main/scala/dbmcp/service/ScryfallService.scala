package dbmcp.service

import cats.effect.IO
import cats.syntax.option.* 
import cats.syntax.either.*
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
import scala.concurrent.duration.*

class ScryfallService(client: Client[IO]):
  val DeckBuilderProductId = ProductId("Deck Builder MCP", "v0.1.0-SNAPSHOT".some)

  def findById(scryfallId: String): IO[Card] =
    val uri = ScryfallUris.findById(scryfallId)
    
    callScryfall(uri)

  def findByName(name: String, setId: Option[SetId], exact: Boolean): IO[Card] =
    val uri = ScryfallUris.findByName(name, setId, exact)

    callScryfall(uri)

  def findBySet(setId: SetId, setNum: SetNum): IO[Card] =
    val uri = ScryfallUris.findBySet(setId, setNum)

    callScryfall(uri)

  def search(query: String, limit: Int = 20): IO[List[Card]] =
    val initialUri = ScryfallUris.search(query)

    pagedScryFallResult[Card](initialUri, limit).map(_._1)

  protected def callScryfall[A: Decoder](uri: Uri): IO[A] =
    val req = scryFallRequest(uri)

    client.expectOr[A](req)(processError)

  protected def pagedScryFallResult[A: Decoder](
    uri: Uri,
    limit: Int = 20,
    dataAcc: List[A] = List.empty,
    warnAcc: Option[List[String]] = None
  ): IO[(List[A], Option[List[String]])] =
    val req = scryFallRequest(uri)

    client.expectOr[ResultPage[A]](req)(processError)
      .flatMap(page => {
        val data = dataAcc ++ page.data
        val warnings =
          if (page.warnings.nonEmpty) 
            Some(warnAcc.getOrElse(List.empty) ++ page.warnings.get)
          else warnAcc
 
        if (data.length >= limit)
          IO.pure((data.take(limit), warnings))
        else
          page.nextPage.fold(IO.pure((data, warnings)))(
            IO.sleep(100.millis) *> pagedScryFallResult[A](_, limit, data, warnings))
      }
    )

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

protected case class ResultPage[A](
  `object`: String,
  data: List[A],
  hasMore: Boolean,
  nextPage: Option[Uri],
  totalCards: Option[Int],
  warnings: Option[List[String]])

protected object ResultPage:
  given Decoder[Uri] = 
    Decoder.decodeString.emap(str => Uri.fromString(str).leftMap(_.message))

  given [A: Decoder]: Decoder[ResultPage[A]] = (hc: HCursor) =>
    for {
      obj <- hc.downField("object").as[String]
      data <- hc.downField("data").as[List[A]]
      hasMore <- hc.downField("has_more").as[Boolean]
      nextPage <- hc.downField("next_page").as[Option[Uri]]
      totalCards <- hc.downField("total_cards").as[Option[Int]]
      warnings <- hc.downField("warnings").as[Option[List[String]]]
    } yield ResultPage(obj, data, hasMore, nextPage, totalCards, warnings)
    
protected case class ScryfallApiError(
  `object`: String,
  code: String,
  `type`: Option[String],
  status: Status,
  warnings: Option[List[String]],
  details: String)

protected object ScryfallApiError:
  given Decoder[Status] = 
    Decoder.decodeInt.emap(i => Status.fromInt(i).leftMap(_.message))

  given Decoder[ScryfallApiError] = (hc: HCursor) =>
    for {
      obj <- hc.downField("object").as[String]
      code <- hc.downField("code").as[String]
      t <- hc.downField("type").as[Option[String]]
      status <- hc.downField("status").as[Status]
      warnings <- hc.downField("warnings").as[Option[List[String]]]
      details <- hc.downField("details").as[String]
    } yield ScryfallApiError(obj, code, t, status, warnings, details)
