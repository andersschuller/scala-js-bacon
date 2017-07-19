import utest._

object EventSuite extends BaseSuite {
  val tests = this {
    "Create Next event from a function" - {
      val value = 2
      val event = new Bacon.Next[Int](() => value)
      assert(event.value() == value)
      assert(event.hasValue())
      assert(event.isNext())
    }

    "Create Next event from a value" - {
      val value = "foo"
      val event = new Bacon.Next(value)
      assert(event.value() == value)
      assert(event.hasValue())
      assert(event.isNext())
    }

    "Create Initial event" - {
      val value = false
      val event = new Bacon.Initial(value)
      assert(event.value() == value)
      assert(event.hasValue())
      assert(event.isInitial())
    }

    "Create End event" - {
      val event = new Bacon.End
      assert(!event.hasValue())
      assert(event.isEnd())
    }

    "Create Error event" - {
      val error = "Something went wrong!"
      val event = new Bacon.Error(error)
      assert(event.error == error)
      assert(!event.hasValue())
      assert(event.isError())
    }
  }
}
