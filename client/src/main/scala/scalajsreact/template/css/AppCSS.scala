package scalajsreact.template.css

import scalajsreact.template.components.{TopNav, LeftNav}
import scalajsreact.template.pages.{HomePage}

import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry
import scalacss.ScalatagsCss
import scalacss.ScalaCssReact


import scalacss.Defaults._

object AppCSS {

  def load = {
    GlobalRegistry.register(
      GlobalStyle,
      TopNav.Style,
      LeftNav.Style
      //,
      //ItemsPage.Style,
      //HomePage.Style
      )
    //GlobalRegistry.addToDocumentOnRegistration()
    //GlobalRegistry.onRegistration(_.addToDocument())
    //GlobalRegistry.addToDocumentOnRegistration()
  }
}