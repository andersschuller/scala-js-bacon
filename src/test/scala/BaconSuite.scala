import org.scalatest.AsyncFunSuite

import scala.concurrent.{ Future, Promise }

class BaconSuite extends AsyncFunSuite {
  test("Create Next event from a function") {
    val value = 2
    val event = new Bacon.Next[Int](() => value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isNext())
  }

  test("Create Next event from a value") {
    val value = "foo"
    val event = new Bacon.Next(value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isNext())
  }

  test("Create Initial event") {
    val value = false
    val event = new Bacon.Initial(value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isInitial())
  }

  test("Create End event") {
    val event = new Bacon.End
    assert(!event.hasValue())
    assert(event.isEnd())
  }

  test("Create Error event") {
    val error = "Something went wrong!"
    val event = new Bacon.Error(error)
    assert(event.error == error)
    assert(!event.hasValue())
    assert(event.isError())
  }

  test("Create EventStream using once") {
    val value = 76
    val stream = Bacon.once(value)
    toFuture(stream).map(values => assert(values == List(value)))
  }

  test("Create EventStream using never") {
    val stream = Bacon.never()
    toFuture(stream).map(values => assert(values == Nil))
  }

  private def toFuture[T](observable: Bacon.Observable[T]): Future[List[T]] = {
    val promise = Promise[List[T]]()
    var values: List[T] = Nil
    observable.onValue { value =>
      values = value :: values
    }
    observable.onEnd { _ =>
      promise.trySuccess(values.reverse)
    }
    observable.onError { error =>
      promise.tryFailure(new Exception(error.error))
    }
    promise.future
  }
}
