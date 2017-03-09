class EventStreamSuite extends BaseSuite {
  override def newInstance = new EventStreamSuite

  test("Create Property from EventStream") {
    val value = Option(3.14)
    val stream = Bacon.once(value)
    val property = stream.toProperty()
    assertContainsValues(property, List(value))
  }

  test("Create Property with initial value from EventStream") {
    val value = 5
    val stream = Bacon.once(value)
    val initialValue = 4
    val property = stream.toProperty(initialValue)
    assertContainsValues(property, List(initialValue, value))
  }

  test("Combine EventStreams with concat") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.concat(secondStream)
    assertContainsValues(combinedStream, List(firstValue, secondValue))
  }

  test("Combine EventStreams with merge") {
    val firstValue = 1
    val secondValue = 2
    val firstStream = Bacon.once(firstValue)
    val secondStream = Bacon.once(secondValue)
    val combinedStream = firstStream.merge(secondStream)
    assertContainsValues(combinedStream, List(firstValue, secondValue))
  }
}
