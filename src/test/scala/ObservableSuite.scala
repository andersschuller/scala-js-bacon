import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

class ObservableSuite extends BaseSuite {
  test("Map over values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("3", "7", "19"))
    val mappedStream = stream.map(_.toInt)
    mappedStream should containValues(List(3, 7, 19))
  }

  test("Map over errors of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
    val mappedStream = stream.mapError(_.toUpperCase)
    mappedStream should containValues(List("a", "ERROR B", "ERROR C"))
  }

  test("Map over end of an Observable") {
    val property = Bacon.constant(2)
    val mappedProperty = property.mapEnd(() => 73)
    mappedProperty should containValues(List(2, 73))
  }

  test("Filter values of an Observable") {
    val stream = Bacon.fromArray[String](js.Array("true", "false", "TRUE", "True", "False"))
    val filteredStream = stream.filter(_.toBoolean)
    filteredStream should containValues(List("true", "TRUE", "True"))
  }

  test("Filter out errors or values of an Observable") {
    def newStream = Bacon.fromArray[Int](js.Array(1, 2, new Bacon.Error("Error!"), 3, 4))
    newStream.skipErrors() should containValues(List(1, 2, 3, 4))
    newStream.skipErrors() should containErrors(List())
    newStream.errors() should containValues(List())
    newStream.errors() should containErrors(List("Error!"))
  }

  test("Take values from an Observable") {
    val values = List[Int | Bacon.Error](1, 2, 3, 4, 5)
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.take(3)
    newStream should containValues(List(1, 2, 3))
  }

  test("Skip values from an Observable") {
    val value = "Some value"
    val property = Bacon.constant(value)
    val newProperty = property.take(10)
    newProperty should containValues(List(value))
  }

  test("Take first value from an Observable") {
    val value = 9.072
    val property = Bacon.constant(value)
    val newProperty = property.first()
    newProperty should containValues(List(value))
  }

  test("Take last value from an Observable") {
    val values = List[Char | Bacon.Error]('x', 'y', 'z')
    val stream = Bacon.fromArray(values.toJSArray)
    val newStream = stream.last()
    newStream should containValues(List('z'))
  }

  test("Create negated Observable using not") {
    val stream = Bacon.fromArray[Boolean](js.Array(false, false, false, true))
    val negatedStream = stream.not()
    negatedStream should containValues(List(true, true, true, false))
  }

  test("Create Property from Observable using scan") {
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 3))
    val property = stream.scan[Int](0, (x, y) => x + y)
    property should containValues(List(0, 1, 3, 6))
  }

  test("Create Property from Observable using fold") {
    val stream = Bacon.fromArray[String](js.Array("H", "e", "ll", "o"))
    val property = stream.fold[String]("", (x, y) => x + y)
    property should containValues(List("Hello"))
  }

  test("Create Property from Observable using reduce") {
    val stream = Bacon.fromArray[Boolean](js.Array(true, false))
    val property = stream.reduce[Boolean](true, (x, y) => x && y)
    property should containValues(List(false))
  }

  test("Create Property from Observable using diff") {
    val stream = Bacon.fromArray[Int](js.Array(1, 4, 9, 16))
    val property = stream.diff[Int](0, (x, y) => y - x)
    property should containValues(List(1, 3, 5, 7))
  }

  test("Create Property from Observable using decode") {
    val property = Bacon.constant('B')
    val stream = Bacon.fromArray[Int](js.Array(1, 2, 1))
    val mapping = js.Dictionary[Char | Bacon.Property[Char]]("1" -> 'A', "2" -> property)
    val decoded = stream.decode(mapping)
    decoded should containValues(List('A'))
  }
}
