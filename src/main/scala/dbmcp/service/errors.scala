package dbmcp.service

sealed abstract class ScryfallError(msg: String) extends Exception(msg)

object ScryfallError:
  case class Ambiguous(details: String) extends ScryfallError(details)
  case class NotFound(details: String) extends ScryfallError(details)
  case class Generic(code: Int, msg: String)
      extends ScryfallError(s"Generic Scryfall error: $msg")
