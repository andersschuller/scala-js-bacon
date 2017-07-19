import org.scalajs.dom
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

object BaconSuite extends BaseSuite {
  val tests = this {
    "Create EventStream using fromPromise" - {
      val value = 97
      val promise = js.Promise.resolve[Int](value)
      val stream = Bacon.fromPromise(promise)
      stream.assertContainsValues(List(value))
    }

    "Create EventStream using fromEvent" - {
      val eventType = "custom"
      val element = dom.document.createElement("div")
      val stream = Bacon.fromEvent[dom.Event](element, eventType).take(1)
      val eventualValues = stream.collectValues

      val event = dom.document.createEvent("Event")
      event.initEvent(eventType, canBubbleArg = true, cancelableArg = true)
      element.dispatchEvent(event)

      eventualValues.assertContains(List(event))
    }

    "Create EventStream using fromCallback" - {
      val stream = Bacon.fromCallback[String] { callback =>
        callback("Bacon!")
      }
      stream.assertContainsValues(List("Bacon!"))
    }

    "Create EventStream using once" - {
      val value = 76
      val stream = Bacon.once(value)
      stream.assertContainsValues(List(value))
    }

    "Create EventStream using once with an error" - {
      val error = "Something bad happened!"
      val stream = Bacon.once(new Bacon.Error(error))
      stream.assertContainsErrors(List(error))
    }

    "Create EventStream using fromArray" - {
      val value = 1
      val error = "fail"
      val values = js.Array[Int | Bacon.Error](value, new Bacon.Error(error))
      assertAll(
        Bacon.fromArray(values).assertContainsValues(List(value)),
        Bacon.fromArray(values).assertContainsErrors(List(error))
      )
    }

    "Create EventStream using repeat" - {
      val stream = Bacon.repeat[Int](i => {
        if (i < 3) {
          Bacon.once(i)
        } else {
          false
        }
      })
      stream.assertContainsValues(List(0, 1, 2))
    }

    "Create EventStream using never" - {
      val stream = Bacon.never()
      stream.assertContainsValues(Nil)
    }

    "Create EventStream using fromBinder" - {
      def newStream = Bacon.fromBinder[String] { sink =>
        sink("first value")
        sink(js.Array[Bacon.Event[String]](new Bacon.Next("2nd"), new Bacon.Next("3rd")))
        sink(new Bacon.Error("oops, an error"))
        sink(new Bacon.End())
        () => ()
      }

      assertAll(
        newStream.assertContainsValues(List("first value", "2nd", "3rd")),
        newStream.assertContainsErrors(List("oops, an error"))
      )
    }

    "Create Property using constant" - {
      val value = "Some data"
      val property = Bacon.constant(value)
      property.assertContainsValues(List(value))
    }

    "Combine Observables and values with combineAsArray" - {
      val streams = List[Int | Bacon.Observable[Int]](7, Bacon.once(8), Bacon.constant(9))
      val property = Bacon.combineAsArray(streams: _*).map(_.toList)
      property.assertContainsValues(List(List(7, 8, 9)))
    }

    "Combine array of Observables and values with combineAsArray" - {
      val streams = js.Array[Int | Bacon.Observable[Int]](7, Bacon.once(8), Bacon.constant(9))
      val property = Bacon.combineAsArray(streams).map(_.toList)
      property.assertContainsValues(List(List(7, 8, 9)))
    }

    "Combine EventStreams with mergeAll" - {
      val values = (1 to 10).toList
      val streams = values.map(Bacon.once[Int])
      val combinedStream = Bacon.mergeAll(streams: _*)
      combinedStream.assertContainsValues(values)
    }

    "Combine array of EventStreams with mergeAll" - {
      val values = (1 to 10).toList
      val streams = values.map(Bacon.once[Int])
      val combinedStream = Bacon.mergeAll(streams.toJSArray)
      combinedStream.assertContainsValues(values)
    }
  }
}
