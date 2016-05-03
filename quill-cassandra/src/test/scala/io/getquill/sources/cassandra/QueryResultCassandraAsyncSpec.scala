package io.getquill.sources.cassandra

import io.getquill._
import monifu.reactive.Observable
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FreeSpec
import org.scalatest.MustMatchers
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class QueryResultTypeCassandraSpec extends FreeSpec with BeforeAndAfterAll with MustMatchers {

  case class OrderTestEntity(id: Int, i: Int)

  val entries = List(
    OrderTestEntity(1, 1),
    OrderTestEntity(2, 2),
    OrderTestEntity(3, 3)
  )

  val insert = quote(query[OrderTestEntity].insert)
  val deleteAll = quote(query[OrderTestEntity].delete)
  val selectAll = quote(query[OrderTestEntity])
  val map = quote(query[OrderTestEntity].map(_.id))
  val filter = quote(query[OrderTestEntity].filter(_.id == 1))
  val withFilter = quote(query[OrderTestEntity].withFilter(_.id == 1))
  val sortBy = quote(query[OrderTestEntity].filter(_.id == 1).sortBy(_.i)(Ord.asc))
  val take = quote(query[OrderTestEntity].take(10))
  val entitySize = quote(query[OrderTestEntity].size)
  val distinct = quote(query[OrderTestEntity].map(_.id).distinct)

  override def beforeAll = {
    val r1 = testSyncDB.run(deleteAll)
    val r2 = testSyncDB.run(insert)(entries)
  }

  "async" - {
    val db = testAsyncDB
    def await[T](r: Future[T]) = Await.result(r, Duration.Inf)
    "return list" - {
      "select" in {
        await(db.run(selectAll)) mustBe a[List[_]]
      }
      "map" in {
        await(db.run(map)) mustBe a[List[_]]
      }
      "filter" in {
        await(db.run(filter)) mustBe a[List[_]]
      }
      "withFilter" in {
        await(db.run(withFilter)) mustBe a[List[_]]
      }
      "sortBy" in {
        await(db.run(sortBy)) mustBe a[List[_]]
      }
      "take" in {
        await(db.run(take)) mustBe a[List[_]]
      }
    }

    "return single result" - {
      "size" in {
        await(db.run(entitySize)) mustEqual entries.size
      }
    }
  }

  "sync" - {
    val db = testSyncDB
    def await[T](r: T) = r
    "return list" - {
      "select" in {
        await(db.run(selectAll)) mustBe a[List[_]]
      }
      "map" in {
        await(db.run(map)) mustBe a[List[_]]
      }
      "filter" in {
        await(db.run(filter)) mustBe a[List[_]]
      }
      "withFilter" in {
        await(db.run(withFilter)) mustBe a[List[_]]
      }
      "sortBy" in {
        await(db.run(sortBy)) mustBe a[List[_]]
      }
      "take" in {
        await(db.run(take)) mustBe a[List[_]]
      }
    }

    "return single result" - {
      "size" in {
        await(db.run(entitySize)) mustEqual entries.size
      }
    }
  }
  "stream" - {
    import monifu.concurrent.Implicits.globalScheduler
    val db = testStreamDB
    def await[T](t: Observable[T]) = {
      val f = t.foldLeft(List.empty[T])(_ :+ _).asFuture
      Await.result(f, Duration.Inf)
    }
    "query" in {
      await(db.run(selectAll)) mustEqual Some(entries)
    }

    "querySingle" in {
      await(db.run(entitySize)) mustEqual Some(List(3))
    }
  }
}
