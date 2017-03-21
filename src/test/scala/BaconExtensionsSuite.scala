import BaconExtensions._
import org.scalajs.dom

class BaconExtensionsSuite extends BaseSuite {
  override def newInstance = new BaconExtensionsSuite

  test("Create EventStream using asEventStream") {
    val eventType = "custom"
    val element = dom.document.createElement("div")
    val stream = element.asEventStream(eventType).take(1)
    val eventualValues = collectValues(stream)

    val event = dom.document.createEvent("Event")
    event.initEvent(eventType, canBubbleArg = true, cancelableArg = true)
    element.dispatchEvent(event)

    eventualValues.map(_ shouldEqual List(event))
  }
}
