import BaconExtensions._
import org.scalajs.dom
import utest._

import scala.concurrent.Promise
import scala.scalajs.js

object BaconExtensionsSuite extends BaseSuite {
  val tests = this {
    "Create EventStream using asEventStream" - {
      val eventType = "custom"
      val element = dom.document.createElement("div")
      val stream = element.asEventStream[dom.Event](eventType).take(1)
      val eventualValues = stream.collectValues

      val event = dom.document.createEvent("Event")
      event.initEvent(eventType, canBubbleArg = true, cancelableArg = true)
      element.dispatchEvent(event)

      eventualValues.assertContains(List(event))
    }

    "Iterate over an Observable using foreach" - {
      val property = Bacon.constant(14)
      val promise = Promise[Int]
      property.foreach(promise.success)
      promise.future.withTimeout.assertContains(14)
    }

    "Flatten nested Observables" - {
      val property = Bacon.constant("foo")
      val stream = Bacon.once(property)
      val flattenedStream = stream.flatten
      flattenedStream.assertContainsValues(List("foo"))
    }

    "Filter values of an Observable using withFilter" - {
      val stream = Bacon.fromArray[String](js.Array("", "a", "b", "", "c"))
      val filteredStream = stream.withFilter(_.nonEmpty)
      filteredStream.assertContainsValues(List("a", "b", "c"))
    }

    "Filter values of an Observable using filterNot" - {
      val stream = Bacon.fromArray[String](js.Array("", "a", "b", "", "c"))
      val filteredStream = stream.filterNot(_.isEmpty)
      filteredStream.assertContainsValues(List("a", "b", "c"))
    }

    "Use Observables in a for comprehension" - {
      val stream = Bacon.fromArray[Int](js.Array(4, 2, 3, 5, 1))

      def squaredStream(i: Int) = Bacon.once(i * i)

      val promise = Promise[(Int, Int)]

      for {
        a <- stream
        b <- squaredStream(a)
        if a + b == 30
      } promise.trySuccess((a, b))

      promise.future.withTimeout.assertContains((5, 25))
    }

    "Use Observables in a for comprehension with yield" - {
      val stream = Bacon.fromArray[Int](js.Array(9, 6, 1, 12))

      def squaredProperty(i: Int) = Bacon.constant(i * i)

      val combinedStream = for {
        a <- stream
        b <- squaredProperty(a)
        if a == b
      } yield (a, b)

      combinedStream.assertContainsValues(List((1, 1)))
    }

    "Filter and map over values of an Observable using collect" - {
      val stream = Bacon.fromArray[Int](js.Array(-5, 2, 17, -3))
      val collectedStream = stream.collect {
        case i if i > 0 => i * 2
      }
      collectedStream.assertContainsValues(List(4, 34))
    }
  }
}
