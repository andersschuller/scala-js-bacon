class SubscriberSuite extends BaseSuite {
  test("Subscribe to Observable") {
    val stream = Bacon.once("Text")
    var result: Option[String] = None
    var error: Option[String] = None

    stream.subscribe {
      case n: Bacon.Next[String] => result = Some(n.value())
      case e: Bacon.Error => error = Some(e.error)
      case _ => ()
    }

    result shouldEqual Some("Text")
    error shouldEqual None
  }

  test("Unsubscribe from Observable") {
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

    values shouldEqual List(1, 2)
  }
}
