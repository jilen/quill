package io.getquill

import io.getquill.ast.{ StablizeLifts, Ast }
import java.util.concurrent.ConcurrentHashMap

object AstCaching {
  private val cache = new ConcurrentHashMap[Ast, Ast]

  def apply(f: Ast => Ast): Ast => Ast = { ori =>
    val p = util.Messages.qprint
    val (stablized, state) = StablizeLifts.stablize(ori)
    val cachedR = cache.get(stablized)
    val normalized = if (cachedR != null) {
      cachedR
    } else {
      val r = f(stablized)
      cache.put(stablized, r)
      r
    }
    StablizeLifts.revert(normalized, state)

  }

}
