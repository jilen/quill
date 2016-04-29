package io.getquill.sources.sql

import io.getquill._

trait QueryResultTypeSpec extends ProductSpec {
  val selectAll = quote(query[Product])
  val map = quote(query[Product].map(_.id))
  val filter = quote(query[Product].filter(_ => true))
  val withFilter = quote(query[Product].withFilter(_ => true))
  val sortBy = quote(query[Product].sortBy(_.id)(Ord.asc))
  val take = quote(query[Product].take(10))
  val drop = quote(query[Product].drop(1))
  val `++` = quote(query[Product] ++ query[Product])
  val unionAll = quote(query[Product].unionAll(query[Product]))
  val union = quote(query[Product].union(query[Product]))

  val minExists = quote(query[Product].map(_.id).min)
  val minNonExists = quote(query[Product].filter(_.id > 1000).map(_.id).min)
  val maxExists = quote(query[Product].map(_.id).min)
  val maxNonExists = quote(query[Product].filter(_.id > 1000).map(_.id).max)
  val avgExists = quote(query[Product].map(_.id).avg)
  val avgNonExists = quote(query[Product].filter(_.id > 1000).map(_.id).avg)
  val productSize = quote(query[Product].size)

  val join = quote(query[Product].join(query[Product]).on(_.id == _.id))
  val rightJoin = quote(query[Product].rightJoin(query[Product]).on(_.id == _.id))
  val fullJoin = quote(query[Product].fullJoin(query[Product]).on(_.id == _.id))
  val selfJoin = quote(query[Product].join(_.id > 0))
  val selfLeftJoin = quote(query[Product].leftJoin(_.id > 0))
  val selfRightJoin = quote(query[Product].rightJoin(_.id > 0))

  val nonEmpty = quote(query[Product].nonEmpty)
  val isEmpty = quote(query[Product].isEmpty)
  val contains = quote(query[Product].map(_.id).contains(0))
  val distinct = quote(query[Product].map(_.id).distinct)

}
