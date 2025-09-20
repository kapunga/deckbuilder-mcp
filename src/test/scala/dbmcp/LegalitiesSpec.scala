package dbmcp

import io.circe.parser._
import munit.FunSuite

class LegalitiesSpec extends FunSuite:

  test("Legalities bit-packing round-trip correctly") {
    val formatMap = Map(
      MtgFormat.Standard -> Legality.Legal,
      MtgFormat.Modern -> Legality.NotLegal,
      MtgFormat.Legacy -> Legality.Restricted,
      MtgFormat.Vintage -> Legality.Banned
    )

    val legalities = Legalities(formatMap)
    val roundTripped = legalities.toFormatMap

    assertEquals(roundTripped(MtgFormat.Standard), Legality.Legal)
    assertEquals(roundTripped(MtgFormat.Modern), Legality.NotLegal)
    assertEquals(roundTripped(MtgFormat.Legacy), Legality.Restricted)
    assertEquals(roundTripped(MtgFormat.Vintage), Legality.Banned)
  }

  test("handle individual format retrieval correctly") {
    val formatMap = Map(MtgFormat.Commander -> Legality.Banned)
    val legalities = Legalities(formatMap)

    assertEquals(legalities.legalityFor(MtgFormat.Commander), Legality.Banned)
    assertEquals(
      legalities.legalityFor(MtgFormat.Standard),
      Legality.Legal
    ) // default
  }

  test("handle mixed legalities correctly") {
    val formatMap = Map(
      MtgFormat.Standard -> Legality.Legal,
      MtgFormat.Pioneer -> Legality.Legal,
      MtgFormat.Modern -> Legality.NotLegal,
      MtgFormat.Legacy -> Legality.Legal,
      MtgFormat.Vintage -> Legality.Restricted,
      MtgFormat.Commander -> Legality.Legal,
      MtgFormat.Pauper -> Legality.Banned
    )

    val legalities = Legalities(formatMap)

    assertEquals(legalities.legalityFor(MtgFormat.Standard), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Pioneer), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Modern), Legality.NotLegal)
    assertEquals(legalities.legalityFor(MtgFormat.Legacy), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Vintage), Legality.Restricted)
    assertEquals(legalities.legalityFor(MtgFormat.Commander), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Pauper), Legality.Banned)
  }

  test("handle empty map correctly") {
    val legalities = Legalities(Map.empty)

    // All formats should default to Legal (ordinal 0)
    MtgFormat.values.foreach { format =>
      assertEquals(legalities.legalityFor(format), Legality.Legal)
    }
  }

  test("handle bit manipulation edge cases") {
    // Test all legalities for first format (should be in least significant bits)
    val testCases = Seq(
      (MtgFormat.Standard, Legality.Legal),
      (MtgFormat.Standard, Legality.NotLegal),
      (MtgFormat.Standard, Legality.Restricted),
      (MtgFormat.Standard, Legality.Banned)
    )

    testCases.foreach { case (format, legality) =>
      val formatMap = Map(format -> legality)
      val legalities = Legalities(formatMap)
      assertEquals(legalities.legalityFor(format), legality)
    }
  }

  test("parse valid Scryfall JSON correctly") {
    val json = """{
      "standard": "legal",
      "future": "legal",
      "historic": "legal",
      "timeless": "legal",
      "gladiator": "legal",
      "pioneer": "legal",
      "modern": "legal",
      "legacy": "legal",
      "pauper": "not_legal",
      "vintage": "legal",
      "penny": "not_legal",
      "commander": "legal",
      "oathbreaker": "legal",
      "standardbrawl": "legal",
      "brawl": "legal",
      "alchemy": "not_legal",
      "paupercommander": "legal",
      "duel": "legal",
      "oldschool": "not_legal",
      "premodern": "not_legal",
      "predh": "not_legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    assert(result.isRight)

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    assertEquals(legalities.legalityFor(MtgFormat.Standard), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Pauper), Legality.NotLegal)
    assertEquals(legalities.legalityFor(MtgFormat.Penny), Legality.NotLegal)
    assertEquals(legalities.legalityFor(MtgFormat.Commander), Legality.Legal)
  }

  test("fail gracefully with unknown format") {
    val json = """{
      "standard": "legal",
      "unknown_format": "legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    assert(result.isLeft)
    result.left.foreach { error =>
      assert(error.getMessage.contains("Unknown format: unknown_format"))
    }
  }

  test("fail gracefully with unknown legality") {
    val json = """{
      "standard": "super_legal",
      "modern": "legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    assert(result.isLeft)
    result.left.foreach { error =>
      assert(error.getMessage.contains("Unknown legality: super_legal"))
    }
  }

  test("handle partial JSON correctly") {
    val json = """{
      "standard": "legal",
      "modern": "not_legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    assert(result.isRight)

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    assertEquals(legalities.legalityFor(MtgFormat.Standard), Legality.Legal)
    assertEquals(legalities.legalityFor(MtgFormat.Modern), Legality.NotLegal)
    assertEquals(
      legalities.legalityFor(MtgFormat.Legacy),
      Legality.Legal
    ) // default
  }

  test("handle empty JSON object") {
    val json = "{}"

    val result = parse(json).flatMap(_.as[Legalities])
    assert(result.isRight)

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    // All formats should default to Legal
    MtgFormat.values.foreach { format =>
      assertEquals(legalities.legalityFor(format), Legality.Legal)
    }
  }
