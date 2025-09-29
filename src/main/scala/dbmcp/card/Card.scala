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

import cats.Show
import cats.syntax.show._
import dbmcp.card.Legalities
import io.circe.{ Decoder, HCursor }

case class CardFace(
    faceType: String,
    name: String,
    manaCost: String,
    typeLine: String,
    text: String,
    colors: List[String],
    power: Option[String],
    toughness: Option[String]
)

object CardFace:
  given Show[CardFace] = (cf: CardFace) =>
    val pt =
      for {
        p <- cf.power
        t <- cf.toughness
      } yield s"${p}/${t}"

    s"""|${cf.name} - ${cf.manaCost}
        |${cf.typeLine}
        |---
        |${cf.text}
        |${pt.getOrElse("")}""".stripMargin

  given Decoder[CardFace] = (c: HCursor) =>
    for {
      faceType <- c
        .downField("object")
        .as[String]
        .map(ft => if (ft == "card") "card_face" else ft)
      name <- c.downField("name").as[String]
      manaCost <- c.downField("mana_cost").as[String]
      typeLine <- c.downField("type_line").as[String]
      text <- c.downField("oracle_text").as[String]
      colors <- c
        .downField("colors")
        .as[Option[List[String]]]
        .map(_.getOrElse(List.empty))
      power <- c.downField("power").as[Option[String]]
      toughness <- c.downField("toughness").as[Option[String]]
    } yield CardFace(
      faceType,
      name,
      manaCost,
      typeLine,
      text,
      colors,
      power,
      toughness
    )

enum Rarity(val name: String):
  case Common extends Rarity("common")
  case Uncommon extends Rarity("uncommon")
  case Rare extends Rarity("rare")
  case Mythic extends Rarity("mythic")

object Rarity:
  given Decoder[Rarity] = Decoder.decodeString.emap { str =>
    Rarity.values.find(_.name == str) match
      case Some(rarity) => Right(rarity)
      case None         => Left(s"Unknown rarity: $str")
  }

case class Card(
    scryfallId: String,
    name: String,
    layout: String,
    rarity: Rarity,
    typeLine: String,
    colorIdentity: List[String],
    cardFaces: List[CardFace],
    legalities: Legalities
)

object Card:
  given Show[Card] = (c: Card) =>
    if (c.cardFaces.size == 1) c.cardFaces.head.show
    else {
      val front = c.cardFaces.head
      val back = c.cardFaces.tail.head

      s"""|${c.name} - ${c.layout}
          |==== Front ====
          |${front.show}
          |==== Back ====
          |${back.show}""".stripMargin
    }

  given Decoder[Card] = (h: HCursor) =>
    for {
      scryfallId <- h.downField("id").as[String]
      name <- h.downField("name").as[String]
      rarity <- h.downField("rarity").as[Rarity]
      layout <- h.downField("layout").as[String]
      typeLine <- h.downField("type_line").as[String]
      colorIdentity <- h.downField("color_identity").as[List[String]]
      cardFaces <- parseFaces(h)
      legalities <- h.downField("legalities").as[Legalities]
    } yield Card(
      scryfallId,
      name,
      layout,
      rarity,
      typeLine,
      colorIdentity,
      cardFaces,
      legalities
    )

  def parseFaces(hc: HCursor): Decoder.Result[List[CardFace]] =
    if (hc.keys.exists(_.exists(_ == "card_faces")))
      hc.downField("card_faces").as[List[CardFace]]
    else
      hc.as[CardFace].map(List(_))
