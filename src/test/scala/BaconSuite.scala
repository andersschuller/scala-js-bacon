import org.scalatest.FunSuite

class BaconSuite extends FunSuite {
  test("Create Next event from a function") {
    val value = 2
    val event = new Bacon.Next[Int](() => value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isNext())
  }

  test("Create Next event from a value") {
    val value = "foo"
    val event = new Bacon.Next(value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isNext())
  }

  test("Create Initial event") {
    val value = false
    val event = new Bacon.Initial(value)
    assert(event.value() == value)
    assert(event.hasValue())
    assert(event.isInitial())
  }

  test("Create End event") {
    val event = new Bacon.End
    assert(!event.hasValue())
    assert(event.isEnd())
  }

  test("Create Error event") {
    val error = "Something went wrong!"
    val event = new Bacon.Error(error)
    assert(event.error == error)
    assert(!event.hasValue())
    assert(event.isError())
  }
}
