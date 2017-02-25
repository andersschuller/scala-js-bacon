import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FunSuite, Matchers }

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

class BaconSuite extends FunSuite with Matchers with BaconMatchers with ScalaFutures {
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

  test("Create EventStream using fromCallback") {
    val stream = Bacon.fromCallback[String] { callback =>
      callback("Bacon!")
    }
    stream should containValues(List("Bacon!"))
  }

  test("Create EventStream using once") {
    val value = 76
    val stream = Bacon.once(value)
    stream should containValues(List(value))
  }

  test("Create EventStream using once with an error") {
    val error = "Something bad happened!"
    val stream = Bacon.once(new Bacon.Error(error))
    stream should containErrors(List(error))
  }

  test("Create EventStream using fromArray") {
    val value = 1
    val error = "fail"
    val values = js.Array[Int | Bacon.Error](value, new Bacon.Error(error))
    Bacon.fromArray(values) should containValues(List(value))
    Bacon.fromArray(values) should containErrors(List(error))
  }

  test("Create EventStream using repeat") {
    val stream = Bacon.repeat[Int](i => {
      if (i < 3) {
        Bacon.once(i)
      } else {
        false
      }
    })
    stream should containValues(List(0, 1, 2))
  }

  test("Create EventStream using never") {
    val stream = Bacon.never()
    stream should containValues(Nil)
  }

  test("Create EventStream using fromBinder") {
    def newStream = Bacon.fromBinder[String] { sink =>
      sink("first value")
      sink(js.Array[Bacon.Event[String]](new Bacon.Next("2nd"), new Bacon.Next("3rd")))
      sink(new Bacon.Error("oops, an error"))
      sink(new Bacon.End())
      () => ()
    }

    newStream should containValues(List("first value", "2nd", "3rd"))
    newStream should containErrors(List("oops, an error"))
  }

  test("Create Property using constant") {
    val value = "Some data"
    val property = Bacon.constant(value)
    property should containValues(List(value))
  }

  test("Create Property from EventStream") {
    val value = Option(3.14)
    val stream = Bacon.once(value)
    val property = stream.toProperty()
    property should containValues(List(value))
  }

  test("Create Property with initial value from EventStream") {
    val value = 5
    val stream = Bacon.once(value)
    val initialValue = 4
    val property = stream.toProperty(initialValue)
    property should containValues(List(initialValue, value))
  }

  test("Create EventStream from Property") {
    val value = BigDecimal("1.23")
    val property = Bacon.constant(value)
    val stream = property.toEventStream()
    stream should containValues(List(value))
  }

  test("Combine EventStreams with concat") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.concat(secondStream)
    combinedStream should containValues(List(firstValue, secondValue))
  }

  test("Combine EventStreams with merge") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.merge(secondStream)
    combinedStream should containValues(List(firstValue, secondValue))
  }

  test("Combine EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams: _*)
    combinedStream should containValues(values)
  }

  test("Combine array of EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams.toJSArray)
    combinedStream should containValues(values)
  }

  test("Combine Properties with and") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.and(falseProperty)
    combinedProperty should containValues(List(false))
  }

  test("Combine Properties with or") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.or(falseProperty)
    combinedProperty should containValues(List(true))
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

  test("Map over values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("3", "7", "19"))
    val mappedStream = stream.map(_.toInt)
    mappedStream should containValues(List(3, 7, 19))
  }

  test("Map over errors of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
    val mappedStream = stream.mapError(_.toUpperCase)
    mappedStream should containValues(List("a", "ERROR B", "ERROR C"))
  }

  test("Map over end of an Observable") {
    val property = Bacon.constant(2)
    val mappedProperty = property.mapEnd(() => 73)
    mappedProperty should containValues(List(2, 73))
  }

  test("Filter values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("true", "false", "TRUE", "True", "False"))
    val filteredStream = stream.filter(_.toBoolean)
    filteredStream should containValues(List("true", "TRUE", "True"))
  }

  test("Filter out errors or values of an Observable") {
    def newStream = Bacon.fromArray[Int](js.Array(1, 2, new Bacon.Error("Error!"), 3, 4))
    newStream.skipErrors() should containValues(List(1, 2, 3, 4))
    newStream.skipErrors() should containErrors(List())
    newStream.errors() should containValues(List())
    newStream.errors() should containErrors(List("Error!"))
  }

  test("Take values from an Observable") {
    val values = List[Int | Bacon.Error](1, 2, 3, 4, 5)
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.take(3)
    newStream should containValues(List(1, 2, 3))
  }

  test("Skip values from an Observable") {
    val value = "Some value"
    val property = Bacon.constant(value)
    val newProperty = property.take(10)
    newProperty should containValues(List(value))
  }

  test("Take first value from an Observable") {
    val value = 9.072
    val property = Bacon.constant(value)
    val newProperty = property.first()
    newProperty should containValues(List(value))
  }

  test("Take last value from an Observable") {
    val values = List[Char | Bacon.Error]('x', 'y', 'z')
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.last()
    newStream should containValues(List('z'))
  }

  test("Create negated Observable using not") {
    val stream = Bacon.fromArray[Boolean](js.Array(false, false, false, true))
    val negatedStream = stream.not()
    negatedStream should containValues(List(true, true, true, false))
  }

  test("Create Property from Observable using scan") {
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 3))
    val property = stream.scan[Int](0, (x, y) => x + y)
    property should containValues(List(0, 1, 3, 6))
  }

  test("Create Property from Observable using fold") {
    val stream = Bacon.fromArray[String](js.Array("H", "e", "ll", "o"))
    val property = stream.fold[String]("", (x, y) => x + y)
    property should containValues(List("Hello"))
  }

  test("Create Property from Observable using reduce") {
    val stream = Bacon.fromArray[Boolean](js.Array(true, false))
    val property = stream.reduce[Boolean](true, (x, y) => x && y)
    property should containValues(List(false))
  }

  test("Create Property from Observable using diff") {
    val stream = Bacon.fromArray[Int](js.Array(1, 4, 9, 16))
    val property = stream.diff[Int](0, (x, y) => y - x)
    property should containValues(List(1, 3, 5, 7))
  }

  test("Subscribe to Observable") {
    val stream = Bacon.once("Text")
    var result: Option[String] = None
    var error: Option[String] = None

    stream.subscribe {
      case n: Bacon.Next[String] => result = Some(n.value())
      case e: Bacon.Error => error = Some(e.error)
      case _ => ()
    }

    result shouldEqual Some("Text")
    error shouldEqual None
  }

  test("Unsubscribe from Observable") {
    val bus = new Bacon.Bus[Int]
    var values: List[Int] = Nil

    val unsubscriber = bus.subscribe {
      case n: Bacon.Next[Int] => values = values :+ n.value()
      case _ => ()
    }

    bus.push(1)
    bus.push(2)
    unsubscriber()
    bus.push(3)
    bus.push(4)

    values shouldEqual List(1, 2)
  }
}
