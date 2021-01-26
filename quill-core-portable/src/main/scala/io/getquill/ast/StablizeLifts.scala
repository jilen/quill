package io.getquill.ast

import scala.collection.immutable.{ Map => IMap }

object StablizeLifts {

  def stablize(ast: Ast): (Ast, State) = {
    val (a, t) = StubLiftValus(State(IMap.empty, 0)).apply(ast)
    (a, t.state)
  }

  def revert(ast: Ast, state: State): Ast = {
    RevertLiftValues(state).apply(ast)
  }

  case class State(
    replaceTable: IMap[Int, Any],
    nextStubId:   Int
  ) {
    def addReplace(id: Int, value: Any): State = {
      this.copy(
        replaceTable = replaceTable + (id -> value),
        nextStubId = nextStubId + 1
      )
    }
  }

  class Stub(val id: Int) extends AnyVal

  case class RevertLiftValues(state: State) extends StatelessTransformer {
    override def apply(ast: Ast): Ast = ast match {
      case l: Lift => applyLift(l)
      case others  => super.apply(others)
    }

    def applyLift(ast: Lift) = ast match {
      case l: ScalarValueLift =>
        val value = state.replaceTable(l.value.asInstanceOf[Stub].id)
        l.copy(value = value)
      case l: ScalarQueryLift =>
        val value = state.replaceTable(l.value.asInstanceOf[Stub].id)
        l.copy(value = value)
      case l: CaseClassValueLift =>
        val value = state.replaceTable(l.value.asInstanceOf[Stub].id)
        l.copy(value = value)
      case l: CaseClassQueryLift =>
        val value = state.replaceTable(l.value.asInstanceOf[Stub].id)
        l.copy(value = value)

    }
  }

  case class StubLiftValus(state: State) extends StatefulTransformer[State] {
    override def apply(e: Ast): (Ast, StatefulTransformer[State]) = e match {
      case l: Lift =>
        val (ast, ss) = applyLift(l)(state)
        (ast, StubLiftValus(state))
      case others =>
        super.apply(others)
    }

    private def applyLift(ast: Lift)(s: State): (Ast, State) = ast match {
      case l: ScalarValueLift =>
        val stub = new Stub(s.nextStubId)
        val stablized = l.copy(value = stub)
        stablized -> state.addReplace(s.nextStubId, stub)
      case l: ScalarQueryLift =>
        val stub = new Stub(s.nextStubId)
        val stablized = l.copy(value = stub)
        stablized -> state.addReplace(s.nextStubId, stub)
      case l: CaseClassValueLift =>
        val stub = new Stub(s.nextStubId)
        val stablized = l.copy(value = stub)
        stablized -> state.addReplace(s.nextStubId, stub)
      case l: CaseClassQueryLift =>
        val stub = new Stub(s.nextStubId)
        val stablized = l.copy(value = stub)
        stablized -> state.addReplace(s.nextStubId, stub)
    }
  }
}
