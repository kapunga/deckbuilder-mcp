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

enum CardFormat(val name: String, val description: String):
  case Normal
      extends CardFormat("normal", "A standard Magic card with one face")
  case Split
      extends CardFormat(
        "split",
        "A card with two faces that can be cast as either side"
      )
  case Flip
      extends CardFormat(
        "flip",
        "Cards that vertically invert with the flip keyword"
      )
  case Transform
      extends CardFormat(
        "transform",
        "Double-sided cards that can change between two sides"
      )
  case ModalDfc
      extends CardFormat(
        "modal_dfc",
        "Double-sided cards that can be played from either side"
      )
  case Meld
      extends CardFormat("meld", "Cards with meld parts printed on the back")
  case Leveler extends CardFormat("leveler", "Cards with Level Up ability")
  case Class extends CardFormat("class", "Class-type enchantment cards")
  case Case extends CardFormat("case", "Case-type enchantment cards")
  case Saga extends CardFormat("saga", "Saga-type cards")
  case Adventure
      extends CardFormat("adventure", "Cards with an Adventure spell part")
  case Mutate extends CardFormat("mutate", "Cards with Mutate ability")
  case Prototype extends CardFormat("prototype", "Cards with Prototype ability")
  case Battle extends CardFormat("battle", "Battle-type cards")
  case Planar extends CardFormat("planar", "Plane and Phenomenon-type cards")
  case Scheme extends CardFormat("scheme", "Scheme-type cards")
  case Vanguard extends CardFormat("vanguard", "Vanguard-type cards")
  case Token extends CardFormat("token", "Token cards")
  case DoubleFacedToken
      extends CardFormat(
        "double_faced_token",
        "Tokens with another token printed on the back"
      )
  case Emblem extends CardFormat("emblem", "Emblem cards")
  case Augment extends CardFormat("augment", "Cards with Augment ability")
  case Host extends CardFormat("host", "Host-type cards")
  case ArtSeries
      extends CardFormat(
        "art_series",
        "Art Series collectable double-faced cards"
      )
  case ReversibleCard
      extends CardFormat("reversible_card", "A card with two unrelated sides")
