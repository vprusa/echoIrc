package scalajsreact.template.css

import scalacss.internal.mutable.GlobalRegistry
import scalajsreact.template.components.TopNav
import scalacss.Defaults._
import scalajsreact.template.pages.IrcChatPage
import scalajsreact.template.pages.TodoPage

object AppCSS {

  def load = {

    GlobalRegistry.register(
      GlobalStyle,
      TopNav.Style,
      IrcChatPage.TargetStyle
    )
    GlobalRegistry.onRegistration(_.addToDocument())
    GlobalRegistry.register(
      //  TodoPage.Style
    )

  }
}