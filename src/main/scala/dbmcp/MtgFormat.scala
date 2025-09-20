package dbmcp

import io.circe.{Decoder, HCursor, JsonObject, DecodingFailure}
import cats.syntax.traverse.*
import cats.syntax.either.*

enum MtgFormat(val name: String):
  case Standard extends MtgFormat("standard")
  case Future extends MtgFormat("future")
  case Historic extends MtgFormat("historic")
  case Timeless extends MtgFormat("timeless")
  case Gladiator extends MtgFormat("gladiator")
  case Pioneer extends MtgFormat("pioneer")
  case Modern extends MtgFormat("modern")
  case Legacy extends MtgFormat("legacy")
  case Pauper extends MtgFormat("pauper")
  case Vintage extends MtgFormat("vintage")
  case Penny extends MtgFormat("penny")
  case Commander extends MtgFormat("commander")
  case Oathbreaker extends MtgFormat("oathbreaker")
  case StandardBrawl extends MtgFormat("standardbrawl")
  case Brawl extends MtgFormat("brawl")
  case Alchemy extends MtgFormat("alchemy")
  case PauperCommander extends MtgFormat("paupercommander")
  case Duel extends MtgFormat("duel")
  case Oldschool extends MtgFormat("oldschool")
  case Premodern extends MtgFormat("premodern")
  case Predh extends MtgFormat("predh")

enum Legality(val name: String):
  // legal, not_legal, restricted, and banned
  case Legal extends Legality("legal")
  case NotLegal extends Legality("not_legal")
  case Restricted extends Legality("restricted")
  case Banned extends Legality("banned")

opaque type Legalities = Long

object Legalities:
  def apply(formatMap: Map[MtgFormat, Legality]): Legalities =
    formatMap.map((f, l) => l.ordinal.toLong << (f.ordinal * 2))
      .foldLeft(0L)(_ | _)

  given Decoder[Legalities] = (c: HCursor) =>
    for {
      jsonObject <- c.as[JsonObject]
      formatMap <- jsonObject.toMap.toList.traverse { case (formatName, legalityJson) =>
        for {
          legalityString <- legalityJson.as[String]
          format <- MtgFormat.values.find(_.name == formatName)
                     .toRight(DecodingFailure(s"Unknown format: $formatName", c.history))
          legality <- Legality.values.find(_.name == legalityString)
                       .toRight(DecodingFailure(s"Unknown legality: $legalityString", c.history))
        } yield format -> legality
      }
    } yield Legalities(formatMap.toMap)

  extension (l: Legalities)
    def toFormatMap: Map[MtgFormat, Legality] =
      MtgFormat.values.map(format => format -> legalityFor(format)).toMap

    def legalityFor(format: MtgFormat): Legality =
      val legalityNum = (l >> (format.ordinal * 2)) & 3
      Legality.fromOrdinal(legalityNum.toInt)
