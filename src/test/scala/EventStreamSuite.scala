class EventStreamSuite extends BaseSuite {
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
}
