package dbmcp.service

import dbmcp.{SetId, SetNum}

import org.http4s.Uri
import org.http4s.implicits.*

object ScryfallUris:
  val baseUri: Uri = uri"https://api.scryfall.com"

  def findByName(name: String, setId: Option[SetId], exact: Boolean): Uri =
    val uri = baseUri / "cards" / "named" +? (if (exact) "exact" else "fuzzy", name)

    setId.fold(uri)(set => uri +? ("set", set))

  def findBySet(setId: SetId, setNum: SetNum): Uri =
    baseUri / setId / setNum
