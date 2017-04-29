import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

class BaconSuite extends BaseSuite {
  override def newInstance = new BaconSuite

  test("Create EventStream using fromPromise") {
    val value = 97
    val promise = js.Promise.resolve[Int](value)
    val stream = Bacon.fromPromise(promise)
    assertContainsValues(stream, List(value))
  }

  test("Create EventStream using fromEvent") {
    val eventType = "custom"
    val element = dom.document.createElement("div")
    val stream = Bacon.fromEvent(element, eventType).take(1)
    val eventualValues = collectValues(stream)

    val event = dom.document.createEvent("Event")
    event.initEvent(eventType, canBubbleArg = true, cancelableArg = true)
    element.dispatchEvent(event)

    eventualValues.map(_ shouldEqual List(event))
  }

  test("Create EventStream using fromCallback") {
    val stream = Bacon.fromCallback[String] { callback =>
      callback("Bacon!")
    }
    assertContainsValues(stream, List("Bacon!"))
  }

  test("Create EventStream using once") {
    val value = 76
    val stream = Bacon.once(value)
    assertContainsValues(stream, List(value))
  }

  test("Create EventStream using once with an error") {
    val error = "Something bad happened!"
    val stream = Bacon.once(new Bacon.Error(error))
    assertContainsErrors(stream, List(error))
  }

  test("Create EventStream using fromArray") {
    val value = 1
    val error = "fail"
    val values = js.Array[Int | Bacon.Error](value, new Bacon.Error(error))
    assertAll(
      assertContainsValues(Bacon.fromArray(values), List(value)),
      assertContainsErrors(Bacon.fromArray(values), List(error))
    )
  }

  test("Create EventStream using repeat") {
    val stream = Bacon.repeat[Int](i => {
      if (i < 3) {
        Bacon.once(i)
      } else {
        false
      }
    })
    assertContainsValues(stream, List(0, 1, 2))
  }

  test("Create EventStream using never") {
    val stream = Bacon.never()
    assertContainsValues(stream, Nil)
  }

  test("Create EventStream using fromBinder") {
    def newStream = Bacon.fromBinder[String] { sink =>
      sink("first value")
      sink(js.Array[Bacon.Event[String]](new Bacon.Next("2nd"), new Bacon.Next("3rd")))
      sink(new Bacon.Error("oops, an error"))
      sink(new Bacon.End())
      () => ()
    }

    assertAll(
      assertContainsValues(newStream, List("first value", "2nd", "3rd")),
      assertContainsErrors(newStream, List("oops, an error"))
    )
  }

  test("Create Property using constant") {
    val value = "Some data"
    val property = Bacon.constant(value)
    assertContainsValues(property, List(value))
  }

  test("Combine Observables and values with combineAsArray") {
    val streams = List[Int | Bacon.Observable[Int]](7, Bacon.once(8), Bacon.constant(9))
    val property = Bacon.combineAsArray(streams: _*).map(_.toList)
    assertContainsValues(property, List(List(7, 8, 9)))
  }

  test("Combine array of Observables and values with combineAsArray") {
    val streams = js.Array[Int | Bacon.Observable[Int]](7, Bacon.once(8), Bacon.constant(9))
    val property = Bacon.combineAsArray(streams).map(_.toList)
    assertContainsValues(property, List(List(7, 8, 9)))
  }

  test("Combine EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams: _*)
    assertContainsValues(combinedStream, values)
  }

  test("Combine array of EventStreams with mergeAll") {
    val values = (1 to 10).toList
    val streams = values.map(Bacon.once[Int])
    val combinedStream = Bacon.mergeAll(streams.toJSArray)
    assertContainsValues(combinedStream, values)
  }
}
