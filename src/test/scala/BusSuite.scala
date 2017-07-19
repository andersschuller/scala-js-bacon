import utest._

object BusSuite extends BaseSuite {
  val tests = this {
    "Push values into a Bus" - {
      val bus = new Bacon.Bus[String]
      val eventualValues = bus.collectValues
      val input = List("First data", "Second data", "Third data")
      input.foreach(bus.push)
      bus.end()
      eventualValues.assertContains(input)
    }

    "Send Error into a Bus" - {
      val bus = new Bacon.Bus[Double]
      val eventualErrors = bus.collectErrors
      val error = "Divide by zero!"
      bus.error(error)
      bus.end()
      eventualErrors.assertContains(List(error))
    }

    "Plug EventStream into a Bus" - {
      val value = 1873
      val stream = Bacon.once(value)
      val bus = new Bacon.Bus[Int]
      val eventualValues = bus.collectValues
      bus.plug(stream)
      bus.end()
      eventualValues.assertContains(List(value))
    }
  }
}
