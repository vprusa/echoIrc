package scalajsreact.template.css

import scalacss.internal.mutable.GlobalRegistry
import scalajsreact.template.components.TopNav
import scalacss.Defaults._
import scalajsreact.template.pages.IrcChatPage

object AppCSS {

  def load = {

    GlobalRegistry.register(
      GlobalStyle,
      TopNav.Style,
      IrcChatPage.TargetStyle
    )
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}