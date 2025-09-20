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
