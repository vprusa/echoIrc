package scalajsreact.template.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.vdom.html_<^.{<, _}
import org.scalajs.dom.ext.Ajax

import scalajsreact.template.models.IrcChatProps
import scalajsreact.template.pages.IrcChatPage.ChatState

object SearchLogs {

  val component =
    ScalaComponent.builder[IrcChatProps]("SearchLogs").render_P(P => {
      <.div(
        <.h3("Searching logs"),

        // get list names and show then as 2 links  (outside and render into page)
        // foreach onclick load log content into div
        // add input button for regex search request on logs
        // - server will have to load and parse log files -> get matching patterns -> add html spans/divs to them with appropriate classes
        // -- classes will be set here and used in example tag
        searchLogsComponent(P)
      )
    }).build

  case class SearchState(var inputRegex: String, var files: Array[String], var logContent: String) {
    def callOnChangeInputRegex($: MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState],
                               props: IrcChatProps)(e: ReactEventFromInput): Callback = {

      val targetInput = e.target.value
      $.modState(state => {
        state.copy(inputRegex = targetInput)
      })
    }

    def callOpenLogFile(props: IrcChatProps, file: String): Option[Callback] = {
      org.scalajs.dom.console.log(s"callOpenLogFile")
      import scala.concurrent.ExecutionContext.Implicits.global
      Ajax.get(s"/rest/getLogFile/${file}").foreach {
        xhr =>
          org.scalajs.dom.console.log(s"callOpenLogFile ${xhr.responseText}")
      }

      Option(Callback.empty)
    }

    def callSearchLogFiles(props: IrcChatProps): Option[Callback] = {
      org.scalajs.dom.console.log(s"callOpenLogFile")
      import scala.concurrent.ExecutionContext.Implicits.global
      Ajax.get(s"/rest/searchLogs/${inputRegex}").foreach {
        xhr =>
          org.scalajs.dom.console.log(s"callOpenLogFile ${xhr.responseText}")
      }

      Option(Callback.empty)
    }

    def setNewFiles(newFiles: Array[String]) = {
      org.scalajs.dom.console.log(s"setNewFiles ${newFiles.toString}")
      org.scalajs.dom.console.log(this.toString)
      newFiles
    }

  }

  class SearchLogsBackend($: BackendScope[IrcChatProps, SearchState]) {

    def render(props: IrcChatProps, s: SearchState) = {
      // http://jmfurlott.github.io/regex-table-filter/

      <.div(
        <.div(
          // TODO https://japgolly.github.io/scalajs-react/#examples/tristate-checkbox
          <.h3("Files"),
          <.ul(
            s.files.map(
              file => {
                <.li(
                  file, " Open: ",
                  <.a(
                    "New tab",
                    ^.target.blank,
                    ^.href := s"${props.restUrl}/getLogFile/${file}"
                  ),
                  " ",
                  <.button(
                    ^.onClick -->? s.callOpenLogFile(props, file), // --> suffixed by ? because it's for Option[Callback]
                    "Here"
                  )
                )
              }).toTagMod
          )
        ),
        <.div(

          <.label("Search regex input:"),
          <.input(
            ^.width := 250.px,
            ^.onChange ==> s.callOnChangeInputRegex($, props),
            ^.value := s.inputRegex
          ),
          <.button(
            ^.onClick -->? s.callSearchLogFiles(props), // --> suffixed by ? because it's for Option[Callback]
            "Search Now"
          ),
          <.label("On input change:"),
          <.input.checkbox(
            // on change
          )
        )
      )
    }


    // to get $ loaded with Everything
    def getDirect(): MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState] = {
      $.withEffectsImpure.asInstanceOf[MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState]]
    }

    def loadLogs() = {
      //f.modState(_.copy())
      //Ajax.get()
      //
      org.scalajs.dom.console.log(s"loadLogs")
      import scala.concurrent.ExecutionContext.Implicits.global
      val futReq = Ajax.get(s"/rest/getLogsNames").foreach {
        xhr => {
          org.scalajs.dom.console.log(s"loadLogs ${xhr.responseText}")
          val newFiles = upickle.default.read[Array[String]](xhr.responseText)
          org.scalajs.dom.console.log(s"loadLogs ${newFiles.toString}")
          var direct = getDirect()
          direct.modState(g => {
            g.copy(files = newFiles)
          })
        }
      }
      Callback(futReq)
      // $.modState(_.copy(files = Array("experimental")))
    }
  }

  val searchLogsComponent = ScalaComponent.builder[IrcChatProps]("EchoLogs")
    .initialState(SearchState("", Array.empty[String], ""))
    // .initialState(ChatState(ListBuffer(defaultTargetStateInside), "", "", None, false))
    .renderBackend[SearchLogsBackend]
    .componentWillMount(_.backend.loadLogs())
    // set username (username has to be as chat state atr because it can be changed via /anick command)
    //    .componentDidMount(f => {
    //f.modState(_.copy())
    //})
    //.(_.backend.end)
    .build

}