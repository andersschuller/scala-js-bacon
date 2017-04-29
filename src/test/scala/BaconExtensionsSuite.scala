import BaconExtensions._
import org.scalajs.dom

import scala.concurrent.Promise
import scala.scalajs.js

class BaconExtensionsSuite extends BaseSuite {
  override def newInstance = new BaconExtensionsSuite

  test("Create EventStream using asEventStream") {
    val eventType = "custom"
    val element = dom.document.createElement("div")
    val stream = element.asEventStream(eventType).take(1)
    val eventualValues = collectValues(stream)

    val event = dom.document.createEvent("Event")
    event.initEvent(eventType, canBubbleArg = true, cancelableArg = true)
    element.dispatchEvent(event)

    eventualValues.map(_ shouldEqual List(event))
  }

  test("Iterate over an Observable using foreach") {
    val property = Bacon.constant(14)
    val promise = Promise[Int]
    property.foreach(promise.success)
    withTimeout(promise.future).map(_ shouldEqual 14)
  }

  test("Flatten nested Observables") {
    val property = Bacon.constant("foo")
    val stream = Bacon.once(property)
    val flattenedStream = stream.flatten
    assertContainsValues(flattenedStream, List("foo"))
  }

  test("Filter values of an Observable using withFilter") {
    val stream = Bacon.fromArray[String](js.Array("", "a", "b", "", "c"))
    val filteredStream = stream.withFilter(_.nonEmpty)
    assertContainsValues(filteredStream, List("a", "b", "c"))
  }

  test("Filter values of an Observable using filterNot") {
    val stream = Bacon.fromArray[String](js.Array("", "a", "b", "", "c"))
    val filteredStream = stream.filterNot(_.isEmpty)
    assertContainsValues(filteredStream, List("a", "b", "c"))
  }

  test("Use Observables in a for comprehension") {
    val stream = Bacon.fromArray[Int](js.Array(4, 2, 3, 5, 1))
    def squaredStream(i: Int) = Bacon.once(i * i)
    val promise = Promise[(Int, Int)]

    for {
      a <- stream
      b <- squaredStream(a)
      if a + b == 30
    } promise.trySuccess((a, b))

    withTimeout(promise.future) map (_ shouldEqual (5, 25))
  }

  test("Use Observables in a for comprehension with yield") {
    val stream = Bacon.fromArray[Int](js.Array(9, 6, 1, 12))
    def squaredProperty(i: Int) = Bacon.constant(i * i)

    val combinedStream = for {
      a <- stream
      b <- squaredProperty(a)
      if a == b
    } yield (a, b)

    assertContainsValues(combinedStream, List((1, 1)))
  }

  test("Filter and map over values of an Observable using collect") {
    val stream = Bacon.fromArray[Int](js.Array(-5, 2, 17, -3))
    val collectedStream = stream.collect {
      case i if i > 0 => i * 2
    }
    assertContainsValues(collectedStream, List(4, 34))
  }
}
