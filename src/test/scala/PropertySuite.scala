import utest._

object PropertySuite extends BaseSuite {
  val tests = this {
    "Create EventStream from Property" - {
      val value = BigDecimal("1.23")
      val property = Bacon.constant(value)
      val stream = property.toEventStream()
      stream.assertContainsValues(List(value))
    }

    "Combine Properties with and" - {
      val trueProperty = Bacon.constant(true)
      val falseProperty = Bacon.constant(false)
      val combinedProperty = trueProperty.and(falseProperty)
      combinedProperty.assertContainsValues(List(false))
    }

    "Combine Properties with or" - {
      val trueProperty = Bacon.constant(true)
      val falseProperty = Bacon.constant(false)
      val combinedProperty = trueProperty.or(falseProperty)
      combinedProperty.assertContainsValues(List(true))
    }
  }
}
