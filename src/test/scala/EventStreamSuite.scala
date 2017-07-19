import utest._

object EventStreamSuite extends BaseSuite {
  val tests = this {
    "Create Property from EventStream" - {
      val value = Option(3.14)
      val stream = Bacon.once(value)
      val property = stream.toProperty()
      property.assertContainsValues(List(value))
    }

    "Create Property with initial value from EventStream" - {
      val value = 5
      val stream = Bacon.once(value)
      val initialValue = 4
      val property = stream.toProperty(initialValue)
      property.assertContainsValues(List(initialValue, value))
    }

    "Combine EventStreams with concat" - {
      val firstValue = 1
      val secondValue = 2
      val firstStream = Bacon.once(firstValue)
      val secondStream = Bacon.once(secondValue)
      val combinedStream = firstStream.concat(secondStream)
      combinedStream.assertContainsValues(List(firstValue, secondValue))
    }

    "Combine EventStreams with merge" - {
      val firstValue = 1
      val secondValue = 2
      val firstStream = Bacon.once(firstValue)
      val secondStream = Bacon.once(secondValue)
      val combinedStream = firstStream.merge(secondStream)
      combinedStream.assertContainsValues(List(firstValue, secondValue))
    }
  }
}
