package io.getquill.sources.cassandra

import scala.annotation.tailrec
import scala.collection.JavaConversions.asScalaBuffer
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import io.getquill.naming.NamingStrategy
import io.getquill.sources.SourceConfig
import io.getquill.CassandraSourceConfig
import io.getquill.sources.BindedStatementBuilder

class CassandraSyncSource[N <: NamingStrategy](config: CassandraSourceConfig[N, CassandraSyncSource[N]])
  extends CassandraSourceSession[N](config) {

  override type QueryResult[T] = List[T]
  override type ActionResult[T] = ResultSet
  override type BatchedActionResult[T] = List[ResultSet]
  override type Params[T] = List[T]

  def query[T](cql: String, bind: BindedStatementBuilder[BoundStatement] => BindedStatementBuilder[BoundStatement], extractor: Row => T): List[T] =
    session.execute(prepare(cql, bind))
      .all.toList.map(extractor)

  def execute(cql: String): ResultSet =
    session.execute(prepare(cql))

  def execute[T](cql: String, bindParams: T => BindedStatementBuilder[BoundStatement] => BindedStatementBuilder[BoundStatement]): List[T] => List[ResultSet] = {
    (values: List[T]) =>
      @tailrec
      def run(values: List[T], acc: List[ResultSet]): List[ResultSet] =
        values match {
          case Nil => List()
          case head :: tail =>
            run(tail, acc :+ session.execute(prepare(cql, bindParams(head))))
        }
      run(values, List.empty)
  }
}
