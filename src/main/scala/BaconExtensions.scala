import org.scalajs.dom

import scala.language.higherKinds

object BaconExtensions {
  implicit class EnrichedEventTarget(target: dom.EventTarget) {
    def asEventStream[T <: dom.Event](eventName: String): Bacon.EventStream[T] = {
      Bacon.fromEvent(target, eventName)
    }
  }

  implicit class EnrichedObservable[T, O[U] <: Bacon.Observable[U]](observable: O[T]) {
    def foreach[U](f: T => U): Unit = {
      observable.onValue(v => f(v))
    }

    def flatten[A](implicit evidence: T <:< Bacon.Observable[A]): Bacon.EventStream[A] = {
      observable.flatMap(t => t)
    }

    def withFilter(p: T => Boolean): O[T]#Self[T] = {
      observable.filter(p)
    }

    def filterNot(p: T => Boolean): O[T]#Self[T] = {
      observable.filter(v => !p(v))
    }

    def collect[A](pf: PartialFunction[T, A]): O[T]#Self[A] = {
      observable.withHandler[A] { (dispatcher, event) =>
        event match {
          case n: Bacon.Next[T] if pf.isDefinedAt(n.value()) =>
            dispatcher.push(new Bacon.Next(pf(n.value())))
          case i: Bacon.Initial[T] if pf.isDefinedAt(i.value()) =>
            dispatcher.push(new Bacon.Initial[A](pf(i.value())))
          case e: Bacon.End =>
            dispatcher.push(e)
          case e: Bacon.Error =>
            dispatcher.push(e)
          case _ =>
            ()
        }
      }
    }
  }
}
