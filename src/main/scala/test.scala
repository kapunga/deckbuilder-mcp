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

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import dbmcp._
import dbmcp.card.Card
import dbmcp.service.ScryfallService
import org.http4s.ember.client.EmberClientBuilder

def lookupById(setId: SetId, setNum: SetNum): String =
  EmberClientBuilder
    .default[IO]
    .build
    .use({ client =>
      ScryfallService(client).findBySet(setId, setNum)
    })
    .unsafeRunSync()
    .show

def lookupByName(
    name: String,
    setId: Option[SetId] = None,
    exact: Boolean = false
): String =
  EmberClientBuilder
    .default[IO]
    .build
    .use({ client =>
      ScryfallService(client).findByName(name, setId, exact)
    })
    .unsafeRunSync()
    .show

def search(query: String, limit: Int = 20): String =
  EmberClientBuilder
    .default[IO]
    .build
    .use({ client =>
      ScryfallService(client).search(query, limit)
    })
    .unsafeRunSync()
    .map(_.show)
    .mkString("\n\n=======\n\n")
