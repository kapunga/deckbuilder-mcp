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

package dbmcp.service

import dbmcp.{ SetId, SetNum }
import org.http4s.Uri
import org.http4s.implicits._

object ScryfallUris:
  val baseUri: Uri = uri"https://api.scryfall.com"

  def findById(scryfallId: String): Uri =
    baseUri / "cards" / scryfallId

  def findByName(name: String, setId: Option[SetId], exact: Boolean): Uri =
    val uri =
      baseUri / "cards" / "named" +? (if (exact) "exact" else "fuzzy", name)

    setId.fold(uri)(set => uri +? ("set", set))

  def findBySet(setId: SetId, setNum: SetNum): Uri =
    baseUri / "cards" / setId / setNum

  def search(query: String): Uri =
    baseUri / "cards" / "search" +? ("q", query)
