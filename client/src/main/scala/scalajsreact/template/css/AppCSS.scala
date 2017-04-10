package scalajsreact.template.css

import scalacss.internal.mutable.GlobalRegistry
import scalajsreact.template.components.TopNav
import scalacss.Defaults._

object AppCSS {

  def load = {

    GlobalRegistry.register(
      GlobalStyle,
      TopNav.Style
    )
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}