package io.getquill.sources.finagle.mysql

import io.getquill.sources.sql._
import com.twitter.util.Await
import com.twitter.util.Future

class QueryResultTypeJdbcSpec extends QueryResultTypeSpec {
  val db = testDB

  def await[T](r: Future[T]) = Await.result(r)

  override def beforeAll = {
    val r1 = await(db.run(deleteAll))
    val r2 = await(db.run(productInsert)(productEntries))
  }

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
    "drop" in {
      await(db.run(drop)) mustBe a[List[_]]
    }
    "++" in {
      await(db.run(`++`)) mustBe a[List[_]]
    }
    "unionAll" in {
      await(db.run(unionAll)) mustBe a[List[_]]
    }
    "union" in {
      await(db.run(union)) mustBe a[List[_]]
    }
    "join" in {
      await(db.run(join)) mustBe a[List[_]]
    }
    "distinct" in {
      await(db.run(distinct)) mustBe a[List[_]]
    }
  }

  "return single result" - {
    "min" - {
      "some" in {
        await(db.run(minExists)) mustBe a[Some[_]]
      }
      "none" in {
        await(db.run(minNonExists)) mustBe None
      }
    }
    "max" - {
      "some" in {
        await(db.run(maxExists)) mustBe a[Some[_]]
      }
      "none" in {
        await(db.run(maxNonExists)) mustBe None
      }
    }
    "avg" - {
      "some" in {
        await(db.run(avgExists)) mustBe a[Some[_]]
      }
      "none" in {
        await(db.run(avgNonExists)) mustBe None
      }
    }
    "size" in {
      await(db.run(productSize)) mustEqual productEntries.size
    }
    "nonEmpty" in {
      await(db.run(nonEmpty)) mustEqual true
    }
    "isEmpty" in {
      await(db.run(isEmpty)) mustEqual false
    }
  }
}
