package dbmcp

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.*

class LegalitiesSpec extends AnyFlatSpec with Matchers:

  "Legalities bit-packing" should "round-trip correctly" in {
    val formatMap = Map(
      MtgFormat.Standard -> Legality.Legal,
      MtgFormat.Modern -> Legality.NotLegal,
      MtgFormat.Legacy -> Legality.Restricted,
      MtgFormat.Vintage -> Legality.Banned
    )

    val legalities = Legalities(formatMap)
    val roundTripped = legalities.toFormatMap

    roundTripped(MtgFormat.Standard) shouldBe Legality.Legal
    roundTripped(MtgFormat.Modern) shouldBe Legality.NotLegal
    roundTripped(MtgFormat.Legacy) shouldBe Legality.Restricted
    roundTripped(MtgFormat.Vintage) shouldBe Legality.Banned
  }

  it should "handle individual format retrieval correctly" in {
    val formatMap = Map(MtgFormat.Commander -> Legality.Banned)
    val legalities = Legalities(formatMap)

    legalities.legalityFor(MtgFormat.Commander) shouldBe Legality.Banned
    legalities.legalityFor(MtgFormat.Standard) shouldBe Legality.Legal // default
  }

  it should "handle mixed legalities correctly" in {
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

    legalities.legalityFor(MtgFormat.Standard) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Pioneer) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Modern) shouldBe Legality.NotLegal
    legalities.legalityFor(MtgFormat.Legacy) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Vintage) shouldBe Legality.Restricted
    legalities.legalityFor(MtgFormat.Commander) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Pauper) shouldBe Legality.Banned
  }

  it should "handle empty map correctly" in {
    val legalities = Legalities(Map.empty)

    // All formats should default to Legal (ordinal 0)
    MtgFormat.values.foreach { format =>
      legalities.legalityFor(format) shouldBe Legality.Legal
    }
  }

  it should "handle bit manipulation edge cases" in {
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
      legalities.legalityFor(format) shouldBe legality
    }
  }

  "Legalities Circe decoder" should "parse valid Scryfall JSON correctly" in {
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
    result.isRight shouldBe true

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    legalities.legalityFor(MtgFormat.Standard) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Pauper) shouldBe Legality.NotLegal
    legalities.legalityFor(MtgFormat.Penny) shouldBe Legality.NotLegal
    legalities.legalityFor(MtgFormat.Commander) shouldBe Legality.Legal
  }

  it should "fail gracefully with unknown format" in {
    val json = """{
      "standard": "legal",
      "unknown_format": "legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    result.isLeft shouldBe true
    result.left.map(_.getMessage should include("Unknown format: unknown_format"))
  }

  it should "fail gracefully with unknown legality" in {
    val json = """{
      "standard": "super_legal",
      "modern": "legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    result.isLeft shouldBe true
    result.left.map(_.getMessage should include("Unknown legality: super_legal"))
  }

  it should "handle partial JSON correctly" in {
    val json = """{
      "standard": "legal",
      "modern": "not_legal"
    }"""

    val result = parse(json).flatMap(_.as[Legalities])
    result.isRight shouldBe true

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    legalities.legalityFor(MtgFormat.Standard) shouldBe Legality.Legal
    legalities.legalityFor(MtgFormat.Modern) shouldBe Legality.NotLegal
    legalities.legalityFor(MtgFormat.Legacy) shouldBe Legality.Legal // default
  }

  it should "handle empty JSON object" in {
    val json = "{}"

    val result = parse(json).flatMap(_.as[Legalities])
    result.isRight shouldBe true

    val legalities = result.getOrElse(fail("Failed to parse JSON"))
    // All formats should default to Legal
    MtgFormat.values.foreach { format =>
      legalities.legalityFor(format) shouldBe Legality.Legal
    }
  }