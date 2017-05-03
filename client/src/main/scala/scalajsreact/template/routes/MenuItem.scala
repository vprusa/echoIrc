package scalajsreact.template.routes

import scalajsreact.template.components.items.{Item1Data, ItemsInfo}
import scalajsreact.template.pages.StatsPage
import japgolly.scalajs.react.extra.router.RouterConfigDsl
import japgolly.scalajs.react.vdom.VdomElement

import scala.collection.mutable.ListBuffer
import scalajsreact.template.models.AppConfig

sealed abstract class MenuItem(val title: String,
                               val routerPath: String,
                               val render: () => VdomElement)


object MenuItem {

  case object Info extends MenuItem("Info", "info", () => ItemsInfo())

  case object Item1 extends MenuItem("Item1", "item1", () => Item1Data())

  case object Item2 extends MenuItem("Item2", "item2", () => Item1Data())

  case class Item3(target: String) extends MenuItem("Item3", "item3", () => Item1Data())

  val menu = ListBuffer[MenuItem](Info, Item1, Item2)

  val routes = RouterConfigDsl[MenuItem].buildRule { dsl =>
    import dsl._
    menu
      .map { i =>
        staticRoute(i.routerPath, i) ~> renderR(
          r => StatsPage(StatsPage.Props(i, r, AppConfig.ircChatPropsTest)))
      }
      .reduce(_ | _)
  }
}