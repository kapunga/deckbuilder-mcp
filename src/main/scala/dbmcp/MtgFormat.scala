package dbmcp

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
