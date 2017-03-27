package io.getquill

import io.getquill.ast._
import io.getquill.context.sql.idiom.QuestionMarkBindVariables
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.idiom.StatementInterpolator._
import java.util.concurrent.atomic.AtomicInteger

trait PostgresDialect
  extends SqlIdiom
  with QuestionMarkBindVariables {

  override implicit def operationTokenizer(implicit propertyTokenizer: Tokenizer[Property], strategy: NamingStrategy): Tokenizer[Operation] =
    Tokenizer[Operation] {
      case UnaryOperation(StringOperator.`toLong`, ast) => stmt"${scopedTokenizer(ast)}::bigint"
      case UnaryOperation(StringOperator.`toInt`, ast)  => stmt"${scopedTokenizer(ast)}::integer"
      case operation                                    => super.operationTokenizer.token(operation)
    }

  override implicit def returningTokenizer(implicit strategy: NamingStrategy): Tokenizer[Returning] = Tokenizer[Returning] {
    case Returning(action: Action, prop, _) => stmt"${action.token} ${strategy.column(prop.name).token}"
  }

  private[getquill] val preparedStatementId = new AtomicInteger

  override def prepareForProbing(string: String) =
    s"PREPARE p${preparedStatementId.incrementAndGet.toString.token} AS $string"
}

object PostgresDialect extends PostgresDialect
