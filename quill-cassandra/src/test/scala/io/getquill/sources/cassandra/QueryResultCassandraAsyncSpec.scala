package io.getquill.sources.cassandra

import io.getquill._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FreeSpec
import org.scalatest.MustMatchers
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class QueryResultTypeCassandraSpec extends FreeSpec with BeforeAndAfterAll with MustMatchers {

  case class TestEntity(id: Int, s: String, i: Int, l: Long, o: Int)

  val entries = List(
    TestEntity(1, "e1", 1, 1L, 1),
    TestEntity(2, "e2", 2, 2L, 2),
    TestEntity(3, "e2", 3, 3L, 3)
  )

  val insert = quote(query[TestEntity].insert)
  val deleteAll = quote(query[TestEntity].delete)
  val selectAll = quote(query[TestEntity])
  val map = quote(query[TestEntity].map(_.id))
  val filter = quote(query[TestEntity].filter(_.id == 1))
  val withFilter = quote(query[TestEntity].withFilter(_.id == 1))
  val sortBy = quote(query[TestEntity].filter(_.id == 1).sortBy(_.id)(Ord.asc))
  val take = quote(query[TestEntity].take(10))
  val union = quote(query[TestEntity].union(query[TestEntity]))
  val entitySize = quote(query[TestEntity].size)
  val distinct = quote(query[TestEntity].map(_.id).distinct)

  override def beforeAll = {
    case class TestEntity(id: Int, s: String, i: Int, l: Long, o: Int)
    val cassandraEntries = List(
      TestEntity(1, "e1", 1, 1L, 1),
      TestEntity(1, "e2", 2, 2L, 2),
      TestEntity(1, "e3", 3, 3L, 3)
    )
    val r1 = testSyncDB.run(query[TestEntity].delete)
    val r2 = testSyncDB.run(query[TestEntity].insert)(cassandraEntries)
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
}
