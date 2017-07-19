import utest._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

object ObservableSuite extends BaseSuite {
  val tests = this {
    "Map over values of an Observable" - {
      val stream = Bacon.fromArray[String](js.Array("3", "7", "19"))
      val mappedStream = stream.map(_.toInt)
      mappedStream.assertContainsValues(List(3, 7, 19))
    }

    "Map over errors of an Observable" - {
      val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
      val mappedStream = stream.mapError(_.toUpperCase)
      mappedStream.assertContainsValues(List("a", "ERROR B", "ERROR C"))
    }

    "Map over end of an Observable" - {
      val property = Bacon.constant(2)
      val mappedProperty = property.mapEnd(() => 73)
      mappedProperty.assertContainsValues(List(2, 73))
    }

    "Filter values of an Observable" - {
      val stream = Bacon.fromArray[String](js.Array("true", "false", "TRUE", "True", "False"))
      val filteredStream = stream.filter(_.toBoolean)
      filteredStream.assertContainsValues(List("true", "TRUE", "True"))
    }

    "Filter out errors or values of an Observable" - {
      def newStream = Bacon.fromArray[Int](js.Array(1, 2, new Bacon.Error("Error!"), 3, 4))

      assertAll(
        newStream.skipErrors().assertContainsValues(List(1, 2, 3, 4)),
        newStream.skipErrors().assertContainsErrors(List()),
        newStream.errors().assertContainsValues(List()),
        newStream.errors().assertContainsErrors(List("Error!"))
      )
    }

    "Flat map over values of an Observable" - {
      val stream = Bacon.fromArray[Int](js.Array(1, 2, 1))

      def repeat[T](value: T)(times: Int): Bacon.EventStream[T] = {
        val values = List.fill[T | Bacon.Error](times)(value)
        Bacon.fromArray(values.toJSArray)
      }

      val flatMappedStream = stream.flatMap(repeat("A"))
      flatMappedStream.assertContainsValues(List("A", "A", "A", "A"))
    }

    "Flat map over errors of an Observable" - {
      val stream = Bacon.fromArray[String](js.Array("a", new Bacon.Error("error b"), new Bacon.Error("error c")))
      val flatMappedStream = stream.flatMapError(error => Bacon.once(error))
      flatMappedStream.assertContainsValues(List("a", "error b", "error c"))
    }

    "Take values from an Observable" - {
      val values = List[Int | Bacon.Error](1, 2, 3, 4, 5)
      val stream = Bacon.fromArray(values.toJSArray)
      val newStream = stream.take(3)
      newStream.assertContainsValues(List(1, 2, 3))
    }

    "Skip values from an Observable" - {
      val value = "Some value"
      val property = Bacon.constant(value)
      val newProperty = property.take(10)
      newProperty.assertContainsValues(List(value))
    }

    "Take first value from an Observable" - {
      val value = 9.072
      val property = Bacon.constant(value)
      val newProperty = property.first()
      newProperty.assertContainsValues(List(value))
    }

    "Take last value from an Observable" - {
      val values = List[Char | Bacon.Error]('x', 'y', 'z')
      val stream = Bacon.fromArray(values.toJSArray)
      val newStream = stream.last()
      newStream.assertContainsValues(List('z'))
    }

    "Create negated Observable using not" - {
      val stream = Bacon.fromArray[Boolean](js.Array(false, false, false, true))
      val negatedStream = stream.not()
      negatedStream.assertContainsValues(List(true, true, true, false))
    }

    "Create Property from Observable using scan" - {
      val stream = Bacon.fromArray[Int](js.Array(1, 2, 3))
      val property = stream.scan[Int](0, (x, y) => x + y)
      property.assertContainsValues(List(0, 1, 3, 6))
    }

    "Create Property from Observable using fold" - {
      val stream = Bacon.fromArray[String](js.Array("H", "e", "ll", "o"))
      val property = stream.fold[String]("", (x, y) => x + y)
      property.assertContainsValues(List("Hello"))
    }

    "Create Property from Observable using reduce" - {
      val stream = Bacon.fromArray[Boolean](js.Array(true, false))
      val property = stream.reduce[Boolean](true, (x, y) => x && y)
      property.assertContainsValues(List(false))
    }

    "Create Property from Observable using diff" - {
      val stream = Bacon.fromArray[Int](js.Array(1, 4, 9, 16))
      val property = stream.diff[Int](0, (x, y) => y - x)
      property.assertContainsValues(List(1, 3, 5, 7))
    }

    "Create Property from Observable using decode" - {
      val property = Bacon.constant('B')
      val stream = Bacon.fromArray[Int](js.Array(1, 2, 1))
      val mapping = js.Dictionary[Char | Bacon.Property[Char]]("1" -> 'A', "2" -> property)
      val decoded = stream.decode(mapping)
      decoded.assertContainsValues(List('A'))
    }

    "Combine Observables using zip" - {
      val firstStream = Bacon.fromArray[Int](js.Array(1, 2))
      val secondStream = Bacon.fromArray[Int](js.Array(3, 4))
      val zippedStream = firstStream.zip(secondStream).map(_.toList)
      zippedStream.assertContainsValues(List(List(1, 3), List(2, 4)))
    }

    "Combine Observables using zip with a function" - {
      val firstStream = Bacon.fromArray[Int](js.Array(1, 2, 3, 4))
      val secondStream = Bacon.fromArray[Char](js.Array('a', 'b', 'c', 'd'))
      val zippedStream = firstStream.zip[Char, String](secondStream, (x, y) => x.toString + y.toString)
      zippedStream.assertContainsValues(List("1a", "2b", "3c", "4d"))
    }

    "Create Promise from last value of Observable" - {
      val values = List[Int | Bacon.Error](1, 2, 3)
      val stream = Bacon.fromArray(values.toJSArray)
      val promise = stream.toPromise()
      promise.toFuture.withTimeout.assertContains(3)
    }

    "Create Promise from first value of Observable" - {
      val value = 'q'
      val property = Bacon.constant(value)
      val promise = property.firstToPromise()
      promise.toFuture.withTimeout.assertContains(value)
    }

    "Implement custom event handling using withHandler" - {
      val stream = Bacon.fromArray[Int](js.Array(76, 19, -5, 8))

      val customStream = stream.withHandler[String] { (dispatcher, event) =>
        event match {
          case n: Bacon.Next[Int] if n.value() > 0 =>
            dispatcher.push(new Bacon.Next(n.value().toString))
          case _ =>
            dispatcher.push(new Bacon.End)
        }
      }

      customStream.assertContainsValues(List("76", "19"))
    }
  }
}
