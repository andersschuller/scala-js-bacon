import scala.language.higherKinds
import scala.scalajs.js
import scala.scalajs.js.|

@js.native
object Bacon extends js.Object {
  type Unsubscriber = js.Function0[Unit]
  type Sink[T] = js.Function1[T | Event[T] | js.Array[Event[T]], Unit]

  def once[T](value: T): EventStream[T] = js.native
  def once[T](error: Error): EventStream[T] = js.native
  def fromArray[T](values: js.Array[T | Error]): EventStream[T] = js.native
  def repeat[T](fn: js.Function1[Int, Observable[T] | Boolean]): EventStream[T] = js.native
  def never(): EventStream[Nothing] = js.native
  def fromBinder[T](f: js.Function1[Sink[T], Unsubscriber]): EventStream[T] = js.native
  def constant[T](value: T): Property[T] = js.native

  def mergeAll[T](streams: EventStream[T]*): EventStream[T] = js.native
  def mergeAll[T](streams: js.Array[EventStream[T]]): EventStream[T] = js.native

  @js.native
  sealed trait Event[+T] extends js.Object {
    def hasValue(): Boolean = js.native
    def isNext(): Boolean = js.native
    def isInitial(): Boolean = js.native
    def isEnd(): Boolean = js.native
    def isError(): Boolean = js.native
  }

  @js.native
  class Next[+T](f: js.Function0[T]) extends Event[T] {
    def this(value: T) = this(() => value)
    def value(): T = js.native
  }

  @js.native
  class Initial[+T](initialValue: T) extends Event[T] {
    def value(): T = js.native
  }

  @js.native
  class End extends Event[Nothing]

  @js.native
  class Error(val error: String) extends Event[Nothing]

  @js.native
  sealed trait Observable[+T] extends js.Object {
    type Self[U] <: Observable[U]

    def subscribe(f: js.Function1[Event[T], Unit]): Unsubscriber = js.native
    def onValue(f: js.Function1[T, Unit]): Unsubscriber = js.native
    def onEnd(f: js.Function0[Unit]): Unsubscriber = js.native
    def onError(f: js.Function1[String, Unit]): Unsubscriber = js.native

    def map[U >: T, A](f: js.Function1[U, A]): Self[A] = js.native
    def mapError[U >: T](f: js.Function1[String, U]): Self[U] = js.native
    def mapEnd[U >: T](f: js.Function0[U]): Self[U] = js.native
    def filter[U >: T](f: js.Function1[U, Boolean]): Self[U] = js.native
    def skipErrors[U >: T](): Self[U] = js.native
    def errors(): Self[Nothing] = js.native

    def take[U >: T](n: Int): Self[U] = js.native
    def skip[U >: T](n: Int): Self[U] = js.native
    def first[U >: T](): Self[U] = js.native
    def last[U >: T](): Self[U] = js.native

    def scan[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def fold[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def reduce[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def diff[A](start: A, f: js.Function2[A, T, A]): Property[A] = js.native

    def log[U >: T](params: js.Any*): Self[U] = js.native
    def doLog[U >: T](params: js.Any*): Self[U] = js.native
  }

  @js.native
  trait EventStream[+T] extends Observable[T] {
    type Self[U] = EventStream[U]

    def toProperty[U >: T](initialValue: U = js.native): Property[U] = js.native

    def concat[U >: T](otherStream: EventStream[U]): EventStream[U] = js.native
    def merge[U >: T](otherStream: EventStream[U]): EventStream[U] = js.native
  }

  @js.native
  trait Property[+T] extends Observable[T] {
    type Self[U] = Property[U]

    def toEventStream[U >: T](): EventStream[U] = js.native

    def and[U >: T](other: Property[U]): Property[U] = js.native
    def or[U >: T](other: Property[U]): Property[U] = js.native
  }

  @js.native
  class Bus[T] extends EventStream[T] {
    def push(value: T): Unit = js.native
    def end(): Unit = js.native
    def error(e: String): Unit = js.native
    def plug(stream: EventStream[T]): Unit = js.native
  }
}
