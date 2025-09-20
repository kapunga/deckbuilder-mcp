package dbmcp.card

import cats.syntax.traverse._
import dbmcp.{ Legality, MtgFormat }
import io.circe._

opaque type Legalities = Long

object Legalities:
  def apply(formatMap: Map[MtgFormat, Legality]): Legalities =
    formatMap
      .map((f, l) => l.ordinal.toLong << (f.ordinal * 2))
      .foldLeft(0L)(_ | _)

  given Decoder[Legalities] = (c: HCursor) =>
    for {
      jsonObject <- c.as[JsonObject]
      formatMap <- jsonObject.toMap.toList.traverse {
        case (formatName, legalityJson) =>
          for {
            legalityString <- legalityJson.as[String]
            format <- MtgFormat.values
              .find(_.name == formatName)
              .toRight(
                DecodingFailure(s"Unknown format: $formatName", c.history)
              )
            legality <- Legality.values
              .find(_.name == legalityString)
              .toRight(
                DecodingFailure(s"Unknown legality: $legalityString", c.history)
              )
          } yield format -> legality
      }
    } yield Legalities(formatMap.toMap)

  extension (l: Legalities)
    def toFormatMap: Map[MtgFormat, Legality] =
      MtgFormat.values.map(format => format -> legalityFor(format)).toMap

    def legalityFor(format: MtgFormat): Legality =
      val legalityNum = (l >> (format.ordinal * 2)) & 3
      Legality.fromOrdinal(legalityNum.toInt)
