package scalajsreact.template.components

import japgolly.scalajs.react.{Callback, _}
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.vdom.html_<^.{<, _}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData

import scalajsreact.template.models.IrcChatProps
import shared.SharedMessages._

import scalajsreact.template.components.SearchLogs.SearchState
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

  case class SearchState(var inputRegex: String, var files: Map[String, Array[String]], var logContent: String, target: String, jsMessagesContent: List[JsMessage]) {
    def callOnChangeInputRegex($: MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState],
                               props: IrcChatProps)(e: ReactEventFromInput): Callback = {

      val targetInput = e.target.value
      $.modState(state => {
        state.copy(inputRegex = targetInput)
      })
    }

    def callSearchLogFiles(props: IrcChatProps): Option[Callback] = {
      org.scalajs.dom.console.log(s"callSearchLogFiles")
      import scala.concurrent.ExecutionContext.Implicits.global
      //val data = InputData.str2ajax(upickle.default.write(JsMessageSearchLogsRequest(regex = inputRegex, target = "*")))
      val inputData = InputData.str2ajax("""{"data":"data"}""")
      //("regex" -> s"${inputRegex}")
      org.scalajs.dom.console.log(s"data")
      org.scalajs.dom.console.log(inputData.toString)
      // TODO error
      Ajax.post(url = s"/rest/searchLogs", data = inputData).foreach {
        xhr => {
          org.scalajs.dom.console.log(s"callSearchLogFiles ${xhr.responseText}")
        }
      }

      Ajax.get(url = s"/rest/searchLogs", data = inputData).foreach {
        xhr => {
          org.scalajs.dom.console.log(s"callSearchLogFiles ${xhr.responseText}")
        }
      }
      Option(Callback.empty)
    }

    def setNewFiles(newFiles: Array[String]) = {
      org.scalajs.dom.console.log(s"setNewFiles ${newFiles.toString}")
      org.scalajs.dom.console.log(this.toString)
      newFiles
    }

  }

  /*
  val renderLine =
    ScalaComponent.builder[JsMessage]("RenderLine")
      .render_P(jsmsg => {
        <.span(
          <.span(
            jsmsg.sender
          ),
          <.span(
            jsmsg.msg
          )
        )
      })
      .build
*/
  def renderLine(jsmsg: JsMessage): VdomElement = <.span(
    ^.display.`inline-block`,
    ^.width := 100.pct,
    <.span(
      ^.paddingRight := 10.px,
      jsmsg.sender,
      ":"
    ),
    <.span(
      jsmsg.msg
    )
  )

  class SearchLogsBackend($: BackendScope[IrcChatProps, SearchState]) {

    def render(props: IrcChatProps, s: SearchState) = {
      // http://jmfurlott.github.io/regex-table-filter/

      <.div(
        <.div(
          // TODO https://japgolly.github.io/scalajs-react/#examples/tristate-checkbox
          <.h3("Files"),
          <.ul(
            s.files.map(
              target => {
                target._2.map(
                  file => {
                    <.li(
                      "Target: ",
                      target._1.toString,
                      " ",
                      file, " Open: ",
                      <.a(
                        "New tab",
                        ^.target.blank,
                        ^.href := s"${props.restUrl}/getLogFile/${target._1.replaceAll("#", "%23")}/${file}"
                      ),
                      " ",
                      <.button(
                        ^.onClick -->? callOpenLogFile(s, props, target._1, file), // --> suffixed by ? because it's for Option[Callback]
                        "Here"
                      )
                    )
                  }).toTagMod
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
          )
          /*,
          <.label("On input change:"),
          <.input.checkbox(
            // on change
          )*/
        ),
        <.div(
          <.h3("Log content"),
          <.div(
            s.jsMessagesContent.toTagMod(renderLine)
            /* s.jsMessagesContent.map(record => {
               <.div("Record",
                 renderLine(record)
               )
             }).toTagMod*/
          )
          //,s.logContent
        )
      )
    }

    // to get $ loaded with Everything
    def getDirect(): MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState] = {
      $.withEffectsImpure.asInstanceOf[MountedWithRoot[CallbackTo, IrcChatProps, SearchState, IrcChatProps, SearchState]]
    }

    def callOpenLogFile(s: SearchState, props: IrcChatProps, target: String, file: String): Option[Callback] = {
      org.scalajs.dom.console.log(s"callOpenLogFile")
      org.scalajs.dom.console.log("target")
      org.scalajs.dom.console.log(target)
      org.scalajs.dom.console.log("file")
      org.scalajs.dom.console.log(file)
      import scala.concurrent.ExecutionContext.Implicits.global
      /*val req = JsMessageGetLogRequest(target, file)
      var data = Ajax.InputData.str2ajax(upickle.default.write(req))
      org.scalajs.dom.console.log(s"data")
      org.scalajs.dom.console.log(data)
      getLogFile/${target._1.replaceAll("#", "%23")}/${file}
      Ajax.get(s"/rest/getLogFile", data).foreach {*/
      val url = s"/rest/getLogFile/${target.replaceAll("#", "%23")}/${file}"
      org.scalajs.dom.console.log("url")
      org.scalajs.dom.console.log(url)
      Ajax.get(url).foreach {
        xhr => {
          var content = ""
          var contentJs = List.empty[JsMessage]
          org.scalajs.dom.console.log(s"callOpenLogFile ${xhr.responseText}")
          xhr.responseText.lines.toList.foreach(line => {
            val logRecord = upickle.default.read[JsMessageBase](line)
            if (logRecord.isInstanceOf[JsMessage]) {
              val messageRecord: JsMessage = logRecord.asInstanceOf[JsMessage]
              content += "Sender: " + messageRecord.sender + " " + messageRecord.msg.toString + "<br>"
              contentJs +:= messageRecord
            }
          })
          org.scalajs.dom.console.log(s"contentJs ${contentJs.toString}")
          //xhr.responseText
          getDirect().modState(g => {
            g.copy(logContent = content, jsMessagesContent = contentJs)
          })
        }
      }
      Option(Callback.empty)
    }

    def loadLogs(): Callback = {
      //f.modState(_.copy())
      //Ajax.get()
      //
      org.scalajs.dom.console.log(s"loadLogs")
      import scala.concurrent.ExecutionContext.Implicits.global
      val futReq = Ajax.get(s"/rest/getAllLogsNames").foreach {
        xhr => {
          org.scalajs.dom.console.log(s"loadLogs ${xhr.responseText}")
          val response = upickle.default.read[JsMessageGetLogsNamesResponse](xhr.responseText)
          org.scalajs.dom.console.log(s"loadLogs ${response.toString}")
          getDirect().modState(g => {
            g.copy(files = response.targetsWithFiles)
          })
        }
      }
      Callback.empty //(futReq)
    }
  }

  val searchLogsComponent = ScalaComponent.builder[IrcChatProps]("EchoLogs")
    .initialState(SearchState("", Map.empty[String, Array[String]], "", "", List.empty[JsMessage]))
    .renderBackend[SearchLogsBackend]
    .componentWillMount(_.backend.loadLogs())
    // set username (username has to be as chat state atr because it can be changed via /anick command)
    .build

}