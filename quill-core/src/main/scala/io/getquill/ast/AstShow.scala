package io.getquill.ast

import io.getquill.ast
import io.getquill.util.Show.Show
import io.getquill.util.Show.Shower
import io.getquill.util.Show.listShow

object AstShow {

  implicit val astShow: Show[Ast] = new Show[Ast] {
    def show(e: Ast) =
      e match {
        case ast: Query           => ast.show
        case ast: Function        => ast.show
        case ast: Value           => ast.show
        case ast: Operation       => ast.show
        case ast: Action          => ast.show
        case ast: Ident           => ast.show
        case ast: Property        => ast.show
        case ast: Infix           => ast.show
        case ast: OptionOperation => ast.show
        case ast: Dynamic         => ast.show
        case ast: If              => ast.show
      }
  }

  implicit val ifShow: Show[If] = new Show[If] {
    def show(q: If) =
      q match {
        case If(a, b, c) => s"if($a) $b else $c"
      }
  }

  implicit val dynamicShow: Show[Dynamic] = new Show[Dynamic] {
    def show(q: Dynamic) =
      q match {
        case Dynamic(tree) => tree.toString
      }
  }

  implicit val queryShow: Show[Query] = new Show[Query] {
    def show(q: Query) =
      q match {

        case q: Entity =>
          q.alias.map(a => s""""$a"""").toList ::: q.properties.map(p => s"""_.${p.property} -> "${p.alias}"""") match {
            case Nil    => s"query[${q.name}]"
            case params => s"query[${q.name}](${params.mkString(", ")})"
          }

        case Filter(source, alias, body) =>
          s"${source.show}.filter(${alias.show} => ${body.show})"

        case Map(source, alias, body) =>
          s"${source.show}.map(${alias.show} => ${body.show})"

        case FlatMap(source, alias, body) =>
          s"${source.show}.flatMap(${alias.show} => ${body.show})"

        case SortBy(source, alias, body, ordering) =>
          s"${source.show}.sortBy(${alias.show} => ${body.show})(${ordering.show})"

        case GroupBy(source, alias, body) =>
          s"${source.show}.groupBy(${alias.show} => ${body.show})"

        case Aggregation(op, ast) =>
          s"${scopedShow(ast)}.${op.show}"

        case Take(source, n) =>
          s"${source.show}.take($n)"

        case Drop(source, n) =>
          s"${source.show}.drop($n)"

        case Union(a, b) =>
          s"${a.show}.union(${b.show})"

        case UnionAll(a, b) =>
          s"${a.show}.unionAll(${b.show})"

        case OuterJoin(t, a, b, iA, iB, on) =>
          s"${a.show}.${t.show}(${b.show}).on((${iA.show}, ${iB.show}) => ${on.show})"
      }
  }

  implicit val orderingShow: Show[Ordering] = new Show[Ordering] {
    def show(q: Ordering) =
      q match {
        case TupleOrdering(elems) => s"Ord(${elems.show})"
        case Asc                  => s"Ord.asc"
        case Desc                 => s"Ord.desc"
        case AscNullsFirst        => s"Ord.ascNullsFirst"
        case DescNullsFirst       => s"Ord.descNullsFirst"
        case AscNullsLast         => s"Ord.ascNullsLast"
        case DescNullsLast        => s"Ord.descNullsLast"
      }
  }

  implicit val optionOperationShow: Show[OptionOperation] = new Show[OptionOperation] {
    def show(q: OptionOperation) = {
      val method = q.t match {
        case OptionMap    => "map"
        case OptionForall => "forall"
        case OptionExists => "exists"
      }
      s"${q.ast}.$method((${q.alias.show}) => ${q.body.show})"
    }
  }

  implicit val outerJoinTypeShow: Show[OuterJoinType] = new Show[OuterJoinType] {
    def show(q: OuterJoinType) =
      q match {
        case LeftJoin  => "leftJoin"
        case RightJoin => "rightJoin"
        case FullJoin  => "fullJoin"
      }
  }

  implicit val functionShow: Show[Function] = new Show[Function] {
    def show(q: Function) =
      q match {
        case Function(params, body) => s"(${params.show}) => ${body.show}"
      }
  }

  implicit val operationShow: Show[Operation] = new Show[Operation] {
    def show(e: Operation) =
      e match {
        case UnaryOperation(op: PrefixUnaryOperator, ast)  => s"${op.show}${scopedShow(ast)}"
        case UnaryOperation(op: PostfixUnaryOperator, ast) => s"${scopedShow(ast)}.${op.show}"
        case BinaryOperation(a, op, b)                     => s"${scopedShow(a)} ${op.show} ${scopedShow(b)}"
        case FunctionApply(function, values)               => s"${scopedShow(function)}.apply(${values.show})"
      }
  }

  implicit def operatorShow[T <: Operator]: Show[T] = new Show[T] {
    def show(o: T) = o.toString
  }

  implicit val propertyShow: Show[Property] = new Show[Property] {
    def show(e: Property) =
      e match {
        case Property(ref, name) => s"${scopedShow(ref)}.$name"
      }
  }

  implicit val valueShow: Show[Value] = new Show[Value] {
    def show(e: Value) =
      e match {
        case Constant(v: String) => s""""$v""""
        case Constant(())        => s"{}"
        case Constant(v)         => s"$v"
        case NullValue           => s"null"
        case Tuple(values)       => s"(${values.show})"
      }
  }

  implicit val identShow: Show[Ident] = new Show[Ident] {
    def show(e: Ident) = e.name
  }

  implicit val actionShow: Show[Action] = new Show[Action] {
    def show(e: Action) =
      e match {
        case AssignedAction(action, assignments) => s"${action.show}(${assignments.show})"
        case Update(query)                       => s"${query.show}.update"
        case Insert(query)                       => s"${query.show}.insert"
        case Delete(query)                       => s"${query.show}.delete"
      }
  }

  implicit val assignmentShow: Show[Assignment] = new Show[Assignment] {
    def show(e: Assignment) =
      e match {
        case Assignment(ident, property, value) => s"$ident => $ident.$property -> ${value.show}"
      }
  }

  implicit val infixShow: Show[Infix] = new Show[Infix] {
    def show(e: Infix) =
      e match {
        case Infix(parts, params) =>
          val ps = params.map(showParam)
          val body = StringContext(parts: _*).s(ps: _*)
          s"""infix"$body""""
      }
    private def showParam(ast: Ast) =
      ast match {
        case ast: Ident => "$" + ast.show
        case other      => "$" + s"{${ast.show}}"
      }
  }

  private def scopedShow(ast: Ast) =
    ast match {
      case _: Function        => s"(${ast.show})"
      case _: BinaryOperation => s"(${ast.show})"
      case other              => ast.show
    }

}
