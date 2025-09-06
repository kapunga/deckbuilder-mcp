package dbmcp

enum Mana(symbol: String):
  case White extends Mana("W")
  case Blue extends Mana("U")
  case Black extends Mana("B")
  case Red extends Mana("R")
  case Green extends Mana("G")
  case Colorless extends Mana("C")

