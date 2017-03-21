import org.scalajs.dom

object BaconExtensions {
  implicit class EnrichedEventTarget(target: dom.EventTarget) {
    def asEventStream[T <: dom.Event](eventName: String): Bacon.EventStream[T] = {
      Bacon.fromEvent(target, eventName)
    }
  }
}
