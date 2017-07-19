import utest._

object SubscriberSuite extends BaseSuite {
  val tests = this {
    "Subscribe to Observable" - {
      val stream = Bacon.once("Text")
      var result: Option[String] = None
      var error: Option[String] = None

      stream.subscribe {
        case n: Bacon.Next[String] => result = Some(n.value())
        case e: Bacon.Error => error = Some(e.error)
        case _ => ()
      }

      assert(result.contains("Text"))
      assert(error.isEmpty)
    }

    "Unsubscribe from Observable" - {
      val bus = new Bacon.Bus[Int]
      var values: List[Int] = Nil

      val unsubscriber = bus.subscribe {
        case n: Bacon.Next[Int] => values = values :+ n.value()
        case _ => ()
      }

      bus.push(1)
      bus.push(2)
      unsubscriber()
      bus.push(3)
      bus.push(4)

      assert(values == List(1, 2))
    }
  }
}
