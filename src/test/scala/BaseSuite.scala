import utest._
import utest.framework.{ Result, Tree }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.scalajs.js.timers.setTimeout

abstract class BaseSuite extends TestSuite {
  import BaseSuite._

  implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override def format(results: Tree[Result]): Option[String] = None

  def assertAll(assertions: Future[Unit]*): Future[Unit] = {
    Future.sequence(assertions).map(_ => ())
  }

  implicit class ObservableTestExtensions[T](observable: Bacon.Observable[T]) {
    def collectValues: Future[List[T]] = {
      val promise = Promise[List[T]]()
      var values = List.empty[T]
      observable.onValue { value =>
        values = value :: values
      }
      observable.onEnd { () =>
        promise.trySuccess(values.reverse)
      }
      promise.future.withTimeout
    }

    def collectErrors: Future[List[String]] = {
      val promise = Promise[List[String]]()
      var errors = List.empty[String]
      observable.onError { error =>
        errors = error :: errors
      }
      observable.onEnd { () =>
        promise.trySuccess(errors.reverse)
      }
      promise.future.withTimeout
    }

    def assertContainsValues(expectedValues: List[T]): Future[Unit] = {
      observable.collectValues.assertContains(expectedValues)
    }

    def assertContainsErrors(expectedErrors: List[String]): Future[Unit] = {
      observable.collectErrors.assertContains(expectedErrors)
    }
  }

  implicit class FutureTestExtensions[T](future: Future[T]) {
    def withTimeout: Future[T] = {
      val promise = Promise[T]
      promise.tryCompleteWith(future)
      setTimeout(futureTimeout) {
        promise.tryFailure(new Exception(s"Future did not complete within $futureTimeout"))
      }
      promise.future
    }

    def assertContains(expectedValue: T): Future[Unit] = {
      future.map(value => assert(value == expectedValue))
    }
  }
}

object BaseSuite {
  private val futureTimeout = 2.seconds
}
