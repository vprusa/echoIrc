package scalajsreact.template.css

import scalacss.Defaults._

/*
  // pallete -
  // http://www.colourlovers.com/palette/338155/Black_and_orange
  // http://www.colourlovers.com/palette/3831583/Orange_and_gray
  // http://www.colourlovers.com/palette/2476685/Black_And_Orange
  // http://www.colourlovers.com/palette/371133/ok_I_admit
  // http://www.colourlovers.com/palette/1480370/upbeat!

*/

object GlobalStyle extends StyleSheet.Inline {

  import dsl._

  style(
    unsafeRoot("body")(
      backgroundColor(c"#2B2B2B"),
      margin.`0`,
      padding.`0`,
      fontSize(14.px),
      color(c"#FFFFFF"),
      fontFamily := "Roboto, sans-serif"
    )
  )
}