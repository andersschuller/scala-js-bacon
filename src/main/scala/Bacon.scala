import scala.language.higherKinds
import scala.scalajs.js
import scala.scalajs.js.|

@js.native
object Bacon extends js.Object {
  type Handler[-T] = js.Function1[T, Unit]
  type Unsubscriber = js.Function0[Unit]
  type Sink[T] = Handler[T | Event[T] | js.Array[Event[T]]]

  def fromPromise[T](promise: js.Promise[T]): EventStream[T] = js.native
  def fromCallback[T](f: Handler[Handler[T]]): EventStream[T] = js.native
  def once[T](value: T): EventStream[T] = js.native
  def once(error: Error): EventStream[Nothing] = js.native
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
    type Self[+U] <: Observable[U]

    def subscribe(f: Handler[Event[T]]): Unsubscriber = js.native
    def onValue(f: Handler[T]): Unsubscriber = js.native
    def onEnd(f: js.Function0[Unit]): Unsubscriber = js.native
    def onError(f: Handler[String]): Unsubscriber = js.native

    def map[A](f: js.Function1[T, A]): Self[A] = js.native
    def mapError[U >: T](f: js.Function1[String, U]): Self[U] = js.native
    def mapEnd[U >: T](f: js.Function0[U]): Self[U] = js.native
    def filter(f: js.Function1[T, Boolean]): Self[T] = js.native
    def skipErrors(): Self[T] = js.native
    def errors(): Self[Nothing] = js.native

    def flatMap[A](f: js.Function1[T, Observable[A]]): EventStream[A] = js.native
    def flatMapError[U >: T](f: js.Function1[String, Observable[U]]): EventStream[U] = js.native

    def take(n: Int): Self[T] = js.native
    def skip(n: Int): Self[T] = js.native
    def first(): Self[T] = js.native
    def last(): Self[T] = js.native
    def not[U >: T]()(implicit evidence: U =:= Boolean): Self[U] = js.native

    def scan[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def fold[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def reduce[A](seed: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def diff[A](start: A, f: js.Function2[A, T, A]): Property[A] = js.native
    def decode[A](mapping: js.Dictionary[A | Property[A]]): Property[A] = js.native

    def toPromise(): js.Promise[T] = js.native
    def firstToPromise(): js.Promise[T] = js.native

    def log(params: js.Any*): Self[T] = js.native
    def doLog(params: js.Any*): Self[T] = js.native
  }

  @js.native
  sealed trait EventStream[+T] extends Observable[T] {
    type Self[+U] = EventStream[U]

    def toProperty[U >: T](initialValue: U = js.native): Property[U] = js.native

    def concat[U >: T](otherStream: EventStream[U]): EventStream[U] = js.native
    def merge[U >: T](otherStream: EventStream[U]): EventStream[U] = js.native
  }

  @js.native
  sealed trait Property[+T] extends Observable[T] {
    type Self[+U] = Property[U]

    def toEventStream(): EventStream[T] = js.native

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
