class PropertySuite extends BaseSuite {
  override def newInstance = new PropertySuite

  test("Create EventStream from Property") {
    val value = BigDecimal("1.23")
    val property = Bacon.constant(value)
    val stream = property.toEventStream()
    assertContainsValues(stream, List(value))
  }

  test("Combine Properties with and") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.and(falseProperty)
    assertContainsValues(combinedProperty, List(false))
  }

  test("Combine Properties with or") {
    val trueProperty = Bacon.constant(true)
    val falseProperty = Bacon.constant(false)
    val combinedProperty = trueProperty.or(falseProperty)
    assertContainsValues(combinedProperty, List(true))
  }
}
