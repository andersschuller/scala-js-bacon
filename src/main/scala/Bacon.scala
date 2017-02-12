import scala.scalajs.js

@js.native
object Bacon extends js.Object {
  @js.native
  sealed trait Event extends js.Object {
    def hasValue(): Boolean = js.native
    def isNext(): Boolean = js.native
    def isInitial(): Boolean = js.native
    def isEnd(): Boolean = js.native
    def isError(): Boolean = js.native
  }

  @js.native
  class Next[+T](f: js.Function0[T]) extends Event {
    def this(value: T) = this(() => value)
    def value(): T = js.native
  }

  @js.native
  class Initial[+T](initialValue: T) extends Event {
    def value(): T = js.native
  }

  @js.native
  class End extends Event

  @js.native
  class Error(val error: String) extends Event
}
