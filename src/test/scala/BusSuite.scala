class BusSuite extends BaseSuite {
  override def newInstance = new BusSuite

  test("Push values into a Bus") {
    val bus = new Bacon.Bus[String]
    val eventualValues = collectValues(bus)
    val input = List("First data", "Second data", "Third data")
    input.foreach(bus.push)
    bus.end()
    eventualValues.map(_ shouldEqual input)
  }

  test("Send Error into a Bus") {
    val bus = new Bacon.Bus[Double]
    val eventualErrors = collectErrors(bus)
    val error = "Divide by zero!"
    bus.error(error)
    bus.end()
    eventualErrors.map(_ shouldEqual List(error))
  }

  test("Plug EventStream into a Bus") {
    val value = 1873
    val stream = Bacon.once(value)
    val bus = new Bacon.Bus[Int]
    val eventualValues = collectValues(bus)
    bus.plug(stream)
    bus.end()
    eventualValues.map(_ shouldEqual List(value))
  }
}
