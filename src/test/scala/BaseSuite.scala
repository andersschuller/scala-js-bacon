import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.scalajs.js.timers.setTimeout

abstract class BaseSuite extends AsyncFunSuite with Matchers with ParallelTestExecution {
  import BaseSuite._

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def collectValues[T](observable: Bacon.Observable[T]): Future[List[T]] = {
    val promise = Promise[List[T]]()
    var values = List.empty[T]
    observable.onValue { value =>
      values = value :: values
    }
    observable.onEnd { () =>
      promise.trySuccess(values.reverse)
    }
    withTimeout(promise.future)
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
    withTimeout(promise.future)
  }

  def assertContainsValues[T](observable: Bacon.Observable[T], expectedValues: List[T]): Future[Assertion] = {
    collectValues(observable).map(_ shouldEqual expectedValues)
  }

  def assertContainsErrors(observable: Bacon.Observable[Any], expectedErrors: List[String]): Future[Assertion] = {
    collectErrors(observable).map(_ shouldEqual expectedErrors)
  }

  def withTimeout[T](future: Future[T]): Future[T] = {
    val promise = Promise[T]
    promise.tryCompleteWith(future)
    setTimeout(futureTimeout) {
      promise.tryFailure(new Exception(s"Future did not complete within $futureTimeout"))
    }
    promise.future
  }

  def assertAll(eventualAssertions: Future[Assertion]*): Future[Assertion] = {
    Future.sequence(eventualAssertions)
      .map(_.find(_ != Succeeded).getOrElse(Succeeded))
  }
}

object BaseSuite {
  private val futureTimeout = 2.seconds
}
