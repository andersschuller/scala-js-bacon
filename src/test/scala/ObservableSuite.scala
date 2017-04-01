import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

class ObservableSuite extends BaseSuite {
  override def newInstance = new ObservableSuite

  test("Map over values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("3", "7", "19"))
    val mappedStream = stream.map(_.toInt)
    assertContainsValues(mappedStream, List(3, 7, 19))
  }

  test("Map over errors of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
    val mappedStream = stream.mapError(_.toUpperCase)
    assertContainsValues(mappedStream, List("a", "ERROR B", "ERROR C"))
  }

  test("Map over end of an Observable") {
    val property = Bacon.constant(2)
    val mappedProperty = property.mapEnd(() => 73)
    assertContainsValues(mappedProperty, List(2, 73))
  }

  test("Filter values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("true", "false", "TRUE", "True", "False"))
    val filteredStream = stream.filter(_.toBoolean)
    assertContainsValues(filteredStream, List("true", "TRUE", "True"))
  }

  test("Filter out errors or values of an Observable") {
    def newStream = Bacon.fromArray[Int](js.Array(1, 2, new Bacon.Error("Error!"), 3, 4))
    assertContainsValues(newStream.skipErrors(), List(1, 2, 3, 4))
    assertContainsErrors(newStream.skipErrors(), List())
    assertContainsValues(newStream.errors(), List())
    assertContainsErrors(newStream.errors(), List("Error!"))
  }

  test("Flat map over values of an Observable") {
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 1))
    def repeat[T](value: T)(times: Int): Bacon.EventStream[T] = {
      val values = List.fill[T | Bacon.Error](times)(value)
      Bacon.fromArray(values.toJSArray)
    }
    val flatMappedStream = stream.flatMap(repeat("A"))
    assertContainsValues(flatMappedStream, List("A", "A", "A", "A"))
  }

  test("Flat map over errors of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
    val flatMappedStream = stream.flatMapError(error => Bacon.once(error))
    assertContainsValues(flatMappedStream, List("a", "error b", "error c"))
  }

  test("Take values from an Observable") {
    val values = List[Int | Bacon.Error](1, 2, 3, 4, 5)
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.take(3)
    assertContainsValues(newStream, List(1, 2, 3))
  }

  test("Skip values from an Observable") {
    val value = "Some value"
    val property = Bacon.constant(value)
    val newProperty = property.take(10)
    assertContainsValues(newProperty, List(value))
  }

  test("Take first value from an Observable") {
    val value = 9.072
    val property = Bacon.constant(value)
    val newProperty = property.first()
    assertContainsValues(newProperty, List(value))
  }

  test("Take last value from an Observable") {
    val values = List[Char | Bacon.Error]('x', 'y', 'z')
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.last()
    assertContainsValues(newStream, List('z'))
  }

  test("Create negated Observable using not") {
    val stream = Bacon.fromArray[Boolean](js.Array(false, false, false, true))
    val negatedStream = stream.not()
    assertContainsValues(negatedStream, List(true, true, true, false))
  }

  test("Create Property from Observable using scan") {
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 3))
    val property = stream.scan[Int](0, (x, y) => x + y)
    assertContainsValues(property, List(0, 1, 3, 6))
  }

  test("Create Property from Observable using fold") {
    val stream = Bacon.fromArray[String](js.Array("H", "e", "ll", "o"))
    val property = stream.fold[String]("", (x, y) => x + y)
    assertContainsValues(property, List("Hello"))
  }

  test("Create Property from Observable using reduce") {
    val stream = Bacon.fromArray[Boolean](js.Array(true, false))
    val property = stream.reduce[Boolean](true, (x, y) => x && y)
    assertContainsValues(property, List(false))
  }

  test("Create Property from Observable using diff") {
    val stream = Bacon.fromArray[Int](js.Array(1, 4, 9, 16))
    val property = stream.diff[Int](0, (x, y) => y - x)
    assertContainsValues(property, List(1, 3, 5, 7))
  }

  test("Create Property from Observable using decode") {
    val property = Bacon.constant('B')
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 1))
    val mapping = js.Dictionary[Char | Bacon.Property[Char]]("1" -> 'A', "2" -> property)
    val decoded = stream.decode(mapping)
    assertContainsValues(decoded, List('A'))
  }

  test("Create Promise from last value of Observable") {
    val values = List[Int | Bacon.Error](1, 2, 3)
    val stream = Bacon.fromArray(values.toJSArray)
    val promise = stream.toPromise()
    withTimeout(promise.toFuture).map(_ shouldEqual values.last)
  }

  test("Create Promise from first value of Observable") {
    val value = 'q'
    val property = Bacon.constant(value)
    val promise = property.firstToPromise()
    withTimeout(promise.toFuture).map(_ shouldEqual value)
  }

  test("Implement custom event handling using withHandler") {
    val stream = Bacon.fromArray[Int](js.Array(76, 19, -5, 8))

    val customStream = stream.withHandler[String] { (dispatcher, event) =>
      event match {
        case n: Bacon.Next[Int] if n.value() > 0 =>
          dispatcher.push(new Bacon.Next(n.value().toString))
        case _ =>
          dispatcher.push(new Bacon.End)
      }
    }

    assertContainsValues(customStream, List("76", "19"))
  }
}
