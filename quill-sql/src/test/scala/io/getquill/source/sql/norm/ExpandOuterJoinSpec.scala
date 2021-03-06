package io.getquill.source.sql.norm

import io.getquill._
import io.getquill.quote
import io.getquill.unquote

class ExpandOuterJoinSpec extends Spec {

  "expands the outer join by mapping the result" - {
    "simple" in {
      val q = quote {
        qr1.leftJoin(qr2).on((a, b) => a.s == b.s)
      }
      ExpandOuterJoin(q.ast).toString mustEqual
        "query[TestEntity].leftJoin(query[TestEntity2]).on((a, b) => a.s == b.s).map(ab => (a, b))"
    }
    "nested" - {
      "left" in {
        val q = quote {
          qr1.leftJoin(qr2).on((a, b) => a.s == b.s).leftJoin(qr3).on((c, d) => c._1.s == d.s)
        }
        ExpandOuterJoin(q.ast).toString mustEqual
          "query[TestEntity].leftJoin(query[TestEntity2]).on((a, b) => a.s == b.s).leftJoin(query[TestEntity3]).on((c, d) => a.s == d.s).map(cd => ((a, b), d))"
      }
      "right" in {
        val q = quote {
          qr1.leftJoin(qr2.leftJoin(qr3).on((a, b) => a.s == b.s)).on((c, d) => c.s == d._1.s)
        }
        ExpandOuterJoin(q.ast).toString mustEqual
          "query[TestEntity].leftJoin(query[TestEntity2].leftJoin(query[TestEntity3]).on((a, b) => a.s == b.s)).on((c, d) => c.s == a.s).map(cd => (c, (a, b)))"
      }
      "both" in {
        val q = quote {
          qr1.leftJoin(qr2).on((a, b) => a.s == b.s).leftJoin(qr3.leftJoin(qr2).on((c, d) => c.s == d.s)).on((e, f) => e._1.s == f._1.s)
        }
        ExpandOuterJoin(q.ast).toString mustEqual
          "query[TestEntity].leftJoin(query[TestEntity2]).on((a, b) => a.s == b.s).leftJoin(query[TestEntity3].leftJoin(query[TestEntity2]).on((c, d) => c.s == d.s)).on((e, f) => a.s == c.s).map(ef => ((a, b), (c, d)))"
      }
    }
  }
}
