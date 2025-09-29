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

package dbmcp.database

import cats.effect.IO
import doobie._
import doobie.implicits._

class CardRepository(xa: Transactor[IO]) {

  def addOwnedCard(card: OwnedCard): IO[Int] = {
    sql"""
      INSERT OR REPLACE INTO owned_cards (set_id, set_number, owned, owned_count, interested)
      VALUES (${card.setId}, ${card.setNumber}, ${card.ownership}, ${card.interested})
    """.update.run.transact(xa)
  }

  def updateCardInterest(
      setId: String,
      setNumber: Int,
      interested: Boolean
  ): IO[Int] = {
    sql"""
      UPDATE owned_cards
      SET interested = $interested
      WHERE set_id = $setId AND set_number = $setNumber
    """.update.run.transact(xa)
  }

  def updateCardOwnership(
      setId: String,
      setNumber: Int,
      ownership: OwnershipStatus
  ): IO[Int] = {
    sql"""
      UPDATE owned_cards
      SET owned = $ownership
      WHERE set_id = $setId AND set_number = $setNumber
    """.update.run.transact(xa)
  }

  def getOwnedCard(setId: String, setNumber: Int): IO[Option[OwnedCard]] = {
    sql"""
      SELECT set_id, set_number, owned, owned_count, interested
      FROM owned_cards
      WHERE set_id = $setId AND set_number = $setNumber
    """.query[OwnedCard].option.transact(xa)
  }

  def listOwnedCards(interestedOnly: Boolean = false): IO[List[OwnedCard]] = {
    val baseQuery =
      sql"SELECT set_id, set_number, owned, owned_count, interested FROM owned_cards"
    val query = if (interestedOnly) {
      baseQuery ++ sql" WHERE interested = 1"
    } else {
      baseQuery
    }
    query.query[OwnedCard].to[List].transact(xa)
  }

  def listOwnedCardsWithStatus(
      ownedOnly: Boolean = true
  ): IO[List[OwnedCard]] = {
    val baseQuery =
      sql"SELECT set_id, set_number, owned, owned_count, interested FROM owned_cards"
    val query = if (ownedOnly) {
      baseQuery ++ sql" WHERE owned = 1"
    } else {
      baseQuery
    }
    query.query[OwnedCard].to[List].transact(xa)
  }

  def removeOwnedCard(setId: String, setNumber: Int): IO[Int] = {
    sql"""
      DELETE FROM owned_cards
      WHERE set_id = $setId AND set_number = $setNumber
    """.update.run.transact(xa)
  }

  // Convenience methods for common ownership operations
  def markAsOwnedUnknownQuantity(setId: String, setNumber: Int): IO[Int] = {
    val card =
      OwnedCard(setId, setNumber, OwnershipStatus.OwnedUnknownQuantity, false)
    addOwnedCard(card)
  }

  def markAsOwnedWithQuantity(
      setId: String,
      setNumber: Int,
      quantity: Int
  ): IO[Int] = {
    val card = OwnedCard(
      setId,
      setNumber,
      OwnershipStatus.OwnedConfirmedQuantity(quantity),
      false
    )
    addOwnedCard(card)
  }

  def markAsNotOwned(setId: String, setNumber: Int): IO[Int] = {
    val card = OwnedCard(setId, setNumber, OwnershipStatus.NotOwned, false)
    addOwnedCard(card)
  }
}
