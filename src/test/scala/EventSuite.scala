class EventSuite extends BaseSuite {
  override def newInstance = new EventSuite

  test("Create Next event from a function") {
    val value = 2
    val event = new Bacon.Next[Int](() => value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isNext() shouldBe true
  }

  test("Create Next event from a value") {
    val value = "foo"
    val event = new Bacon.Next(value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isNext() shouldBe true
  }

  test("Create Initial event") {
    val value = false
    val event = new Bacon.Initial(value)
    event.value() shouldEqual value
    event.hasValue() shouldBe true
    event.isInitial() shouldBe true
  }

  test("Create End event") {
    val event = new Bacon.End
    event.hasValue() shouldBe false
    event.isEnd() shouldBe true
  }

  test("Create Error event") {
    val error = "Something went wrong!"
    val event = new Bacon.Error(error)
    event.error shouldEqual error
    event.hasValue() shouldBe false
    event.isError() shouldBe true
  }
}
