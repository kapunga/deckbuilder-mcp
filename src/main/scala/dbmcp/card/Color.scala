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

enum Color(val ch: Char, val flag: Int):
  case White extends Color('W', 0b00001)
  case Blue extends Color('U', 0b00010)
  case Black extends Color('B', 0b00100)
  case Red extends Color('R', 0b01000)
  case Green extends Color('G', 0b10000)

enum ColorCombo(val colors: Set[Color]):
  override def toString(): String = colors.map(_.ch).mkString
  def flags: Int = colors.map(_.flag).fold(0)(_ | _)

  case Colorless extends ColorCombo(Set.empty)
  case White extends ColorCombo(Set(Color.White))
  case Blue extends ColorCombo(Set(Color.Blue))
  case Black extends ColorCombo(Set(Color.Black))
  case Red extends ColorCombo(Set(Color.Red))
  case Green extends ColorCombo(Set(Color.Green))
  case Azorius extends ColorCombo(Set(Color.White, Color.Blue))
  case Dimir extends ColorCombo(Set(Color.Blue, Color.Black))
  case Rakdos extends ColorCombo(Set(Color.Black, Color.Red))
  case Gruul extends ColorCombo(Set(Color.Red, Color.Green))
  case Selesnya extends ColorCombo(Set(Color.Green, Color.White))
  case Orzhov extends ColorCombo(Set(Color.White, Color.Black))
  case Izzet extends ColorCombo(Set(Color.Blue, Color.Red))
  case Golgari extends ColorCombo(Set(Color.Black, Color.Green))
  case Boros extends ColorCombo(Set(Color.Red, Color.White))
  case Simic extends ColorCombo(Set(Color.Green, Color.Blue))
  case Bant extends ColorCombo(Set(Color.White, Color.Green, Color.Blue))
  case Esper extends ColorCombo(Set(Color.Blue, Color.White, Color.Black))
  case Grixis extends ColorCombo(Set(Color.Black, Color.Red, Color.Blue))
  case Jund extends ColorCombo(Set(Color.Red, Color.Black, Color.Green))
  case Naya extends ColorCombo(Set(Color.Green, Color.Red, Color.White))
  case Abzan extends ColorCombo(Set(Color.White, Color.Black, Color.Green))
  case Jeskai extends ColorCombo(Set(Color.Blue, Color.Red, Color.White))
  case Sultai extends ColorCombo(Set(Color.Black, Color.Green, Color.Blue))
  case Mardu extends ColorCombo(Set(Color.Red, Color.White, Color.Black))
  case Temur extends ColorCombo(Set(Color.Green, Color.Red, Color.Blue))
  case Chaos
      extends ColorCombo(Set(Color.Blue, Color.Black, Color.Red, Color.Green))
  case Aggression
      extends ColorCombo(Set(Color.Black, Color.Red, Color.Green, Color.White))
  case Altruism
      extends ColorCombo(Set(Color.Red, Color.Green, Color.White, Color.Blue))
  case Growth
      extends ColorCombo(Set(Color.Green, Color.White, Color.Blue, Color.Black))
  case Artifice
      extends ColorCombo(Set(Color.White, Color.Blue, Color.Black, Color.Red))
  case Domain
      extends ColorCombo(
        Set(Color.White, Color.Blue, Color.Black, Color.Red, Color.Green)
      )

object ColorCombo:
  lazy val stringMap: Map[String, ColorCombo] =
    ColorCombo.values.map(cc => cc.toString -> cc).toMap

  lazy val flagMap: Map[Int, ColorCombo] =
    ColorCombo.values.map(cc => cc.flags -> cc).toMap

  def apply(flags: Int): ColorCombo =
    val colors = Color.values.toSet.filter(c => (flags & c.flag) > 0)
    ColorCombo.values.find(_.colors == colors).getOrElse(Colorless)

  def apply(colors: String): ColorCombo =
    val charSet = colors.toUpperCase.toSet
    val colorSet = Color.values.toSet.filter(c => charSet.contains(c.ch))
    ColorCombo.values.find(_.colors == colorSet).getOrElse(Colorless)
