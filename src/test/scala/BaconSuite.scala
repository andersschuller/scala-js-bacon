import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FunSuite, Matchers }

import scala.concurrent.{ Future, Promise }
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

class BaconSuite extends FunSuite with Matchers with ScalaFutures {
  test("Create Next event from a function") {
    val value = 2
    val event = new Bacon.Next[Int](() => value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isNext() shouldBe true
  }

  test("Create Next event from a value") {
    val value = "foo"
    val event = new Bacon.Next(value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isNext() shouldBe true
  }

  test("Create Initial event") {
    val value = false
    val event = new Bacon.Initial(value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isInitial() shouldBe true
  }

  test("Create End event") {
    val event = new Bacon.End
    event.hasValue() shouldBe false
    event.isEnd() shouldBe true
  }

  test("Create Error event") {
    val error = "Something went wrong!"
    val event = new Bacon.Error(error)
    event.error shouldEqual error
    event.hasValue() shouldBe false
    event.isError() shouldBe true
  }

  test("Create EventStream using once") {
    val value = 76
    val stream = Bacon.once(value)
    collectValues(stream).futureValue shouldEqual List(value)
  }

  test("Create EventStream using once with an error") {
    val error = "Something bad happened!"
    val stream = Bacon.once[Double](new Bacon.Error(error))
    collectErrors(stream).futureValue shouldEqual List(error)
  }

  test("Create EventStream using fromArray") {
    val value = 1
    val error = "fail"
    val values = js.Array[Int | Bacon.Error](value, new Bacon.Error(error))
    collectValues(Bacon.fromArray(values)).futureValue shouldEqual List(value)
    collectErrors(Bacon.fromArray(values)).futureValue shouldEqual List(error)
  }

  test("Create EventStream using never") {
    val stream = Bacon.never()
    collectValues(stream).futureValue shouldEqual Nil
  }

  test("Create Property using constant") {
    val value = "Some data"
    val property = Bacon.constant(value)
    collectValues(property).futureValue shouldEqual List(value)
  }

  test("Create Property from EventStream") {
    val value = Option(3.14)
    val stream = Bacon.once(value)
    val property = stream.toProperty()
    collectValues(property).futureValue shouldEqual List(value)
  }

  test("Create Property with initial value from EventStream") {
    val value = 5
    val stream = Bacon.once(value)
    val initialValue = 4
    val property = stream.toProperty(initialValue)
    collectValues(property).futureValue shouldEqual List(initialValue, value)
  }

  test("Create EventStream from Property") {
    val value = BigDecimal("1.23")
    val property = Bacon.constant(value)
    val stream = property.toEventStream()
    collectValues(stream).futureValue shouldEqual List(value)
  }

  test("Combine EventStreams with concat") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.concat(secondStream)
    collectValues(combinedStream).futureValue shouldEqual List(firstValue, secondValue)
  }

  test("Combine EventStreams with merge") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.merge(secondStream)
    collectValues(combinedStream).futureValue shouldEqual List(firstValue, secondValue)
  }

  test("Combine EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams: _*)
    collectValues(combinedStream).futureValue shouldEqual values
  }

  test("Combine array of EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams.toJSArray)
    collectValues(combinedStream).futureValue shouldEqual values
  }

  test("Combine Properties with and") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.and(falseProperty)
    collectValues(combinedProperty).futureValue shouldEqual List(false)
  }

  test("Combine Properties with or") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.or(falseProperty)
    collectValues(combinedProperty).futureValue shouldEqual List(true)
  }

  test("Push values into a Bus") {
    val bus = new Bacon.Bus[String]
    val eventualValues = collectValues(bus)
    val input = List("First data", "Second data", "Third data")
    input.foreach(bus.push)
    bus.end()
    eventualValues.futureValue shouldEqual input
  }

  test("Send Error into a Bus") {
    val bus = new Bacon.Bus[Double]
    val eventualFailure = collectErrors(bus)
    val error = "Divide by zero!"
    bus.error(error)
    bus.end()
    eventualFailure.futureValue shouldEqual List(error)
  }

  test("Plug EventStream into a Bus") {
    val value = 1873
    val stream = Bacon.once(value)
    val bus = new Bacon.Bus[Int]
    val eventualValues = collectValues(bus)
    bus.plug(stream)
    bus.end()
    eventualValues.futureValue shouldEqual List(value)
  }

  private def collectValues[T](observable: Bacon.Observable[T]): Future[List[T]] = {
    val promise = Promise[List[T]]()
    var values: List[T] = Nil
    observable.onValue { value =>
      values = value :: values
    }
    observable.onEnd { () =>
      promise.trySuccess(values.reverse)
    }
    promise.future
  }

  private def collectErrors[T](observable: Bacon.Observable[T]): Future[List[String]] = {
    val promise = Promise[List[String]]()
    var errors: List[String] = Nil
    observable.onError { error =>
      errors = error :: errors
    }
    observable.onEnd { () =>
      promise.trySuccess(errors.reverse)
    }
    promise.future
  }
}
