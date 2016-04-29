package io.getquill.sources.jdbc

import io.getquill.sources.sql._

class QueryResultTypeJdbcSpec extends QueryResultTypeSpec {
  val db = h2.testH2DB

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
    "leftJoin" in {
      await(db.run(leftJoin)) mustBe a[List[_]]
    }
    "rightJoin" in {
      await(db.run(rightJoin)) mustBe a[List[_]]
    }
    "fullJoin" in {
      await(db.run(fullJoin)) mustBe a[List[_]]
    }
    "selfJoin" in {
      await(db.run(selfJoin)) mustBe a[List[_]]
    }
    "selfLeftJoin" in {
      await(db.run(selfLeftJoin)) mustBe a[List[_]]
    }
    "selfRightJoin" in {
      await(db.run(selfRightJoin)) mustBe a[List[_]]
    }
  }

  "return single result" - {
    "min" - {
      "some" in {
        await(db.run(minExists)) must not be None
      }
      "none" in {
        await(db.run(minExists)) mustBe None
      }
    }
  }
}
