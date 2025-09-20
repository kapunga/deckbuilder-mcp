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
