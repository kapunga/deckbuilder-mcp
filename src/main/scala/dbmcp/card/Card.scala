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

case class Card(
    scryfallId: String,
    name: String,
    layout: String,
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
      layout <- h.downField("layout").as[String]
      typeLine <- h.downField("type_line").as[String]
      colorIdentity <- h.downField("color_identity").as[List[String]]
      cardFaces <- parseFaces(h)
      legalities <- h.downField("legalities").as[Legalities]
    } yield Card(
      scryfallId,
      name,
      layout,
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
