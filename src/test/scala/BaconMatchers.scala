import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.{ MatchResult, Matcher }

import scala.concurrent.{ Future, Promise }

trait BaconMatchers {
  self: ScalaFutures =>

  def collectValues[T](observable: Bacon.Observable[T]): Future[List[T]] = {
    val promise = Promise[List[T]]()
    var values = List.empty[T]
    observable.onValue { value =>
      values = value :: values
    }
    observable.onEnd { () =>
      promise.trySuccess(values.reverse)
    }
    promise.future
  }

  def collectErrors(observable: Bacon.Observable[Any]): Future[List[String]] = {
    val promise = Promise[List[String]]()
    var errors = List.empty[String]
    observable.onError { error =>
      errors = error :: errors
    }
    observable.onEnd { () =>
      promise.trySuccess(errors.reverse)
    }
    promise.future
  }

  def containValues[T](expectedValues: List[T]): Matcher[Bacon.Observable[T]] = {
    new ObservableValuesMatcher(expectedValues)
  }

  def containErrors(expectedErrors: List[String]): Matcher[Bacon.Observable[Any]] = {
    new ObservableErrorsMatcher(expectedErrors)
  }

  private class ObservableValuesMatcher[T](expectedValues: List[T]) extends Matcher[Bacon.Observable[T]] {
    override def apply(observable: Bacon.Observable[T]): MatchResult = {
      val values = collectValues(observable).futureValue
      MatchResult(
        values == expectedValues,
        s"The observable $observable contained values $values instead of the expected values $expectedValues",
        s"The observable $observable contained values $expectedValues"
      )
    }
  }

  private class ObservableErrorsMatcher(expectedErrors: List[String]) extends Matcher[Bacon.Observable[Any]] {
    override def apply(observable: Bacon.Observable[Any]): MatchResult = {
      val errors = collectErrors(observable).futureValue
      MatchResult(
        errors == expectedErrors,
        s"The observable $observable contained errors $errors instead of the expected errors $expectedErrors",
        s"The observable $observable contained errors $expectedErrors"
      )
    }
  }
}
