package dbmcp

type SetId = String
type SetNum = Int

case class CardInSet(set: SetId, collectionNumber: SetNum)

