package scalajsreact.template.css

import scalacss.Defaults._

object MyStyles extends StyleSheet.Inline {
  import dsl._

  val bootstrapButton = style(
    addClassName("btn btn-default"),
    fontSize(200 %%)
  )
}