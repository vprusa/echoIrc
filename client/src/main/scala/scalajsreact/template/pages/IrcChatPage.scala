package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom._
import shared.SharedMessages._
import upickle.default.{read, _}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}
import scalajsreact.template.models.IrcChatProps

object IrcChatPage {

  // contains live info about channel
  case class TargetStateInside(var logLines: Vector[JsMessage], message: String) {
  }

  // contains passive info about channel with pointer to live info
  case class TargetProps(target: String, password: String, tsi: TargetStateInside) {
  }

  case class TargetWrapper(targetState: TargetProps, chatState: ChatState, ircChatProps: IrcChatProps) {
  }

  val defaultTargetStateInside = TargetStateInside(logLines = Vector.empty[JsMessage], message = "")
  val defaultTargetProps = TargetProps(target = "#TheName", password = "", defaultTargetStateInside)

  case class ChatState(ws: Option[WebSocket], var targets: ListBuffer[TargetProps], sender: String, channelJoin: String /*, channelJoinPassword : String*/) {
    // Create a new state with a line added to the log
    def logLine(line: String): ChatState = {
      org.scalajs.dom.console.log("state " + this)
      this
    }

    def logTargetLine(msg: JsMessage): ChatState = {
      org.scalajs.dom.console.log("logTargetLine ")
      var state: ChatState = this
      if (targets.isEmpty) {}
      org.scalajs.dom.console.log("targets " + targets)

      targets.filter(_.target == msg.target).foreach(f => {
        f.tsi.logLines = f.tsi.logLines :+ msg
      })

      state = copy(targets = targets)

      org.scalajs.dom.console.log("logTargetLine last state")
      org.scalajs.dom.console.log(state.toString)

      state
    }
  }

  class ChannelBackend($c: BackendScope[TargetWrapper, TargetStateInside]) {

    def onChangeChannelInput(e: ReactEventFromInput): Callback = {
      val newMessage = e.target.value
      org.scalajs.dom.console.log("onChangeChannelInput " + newMessage)

      $c.modState(_.copy(message = newMessage))
    }

    def sendChatMessage2Target(ws: WebSocket, targetWrapper: TargetWrapper, tsi: TargetStateInside): Callback = {
      // Send a message to the WebSocket
      org.scalajs.dom.console.log("sendChatMessage2Target")

      val msg: JsMessage = JsMessage(sender = targetWrapper.chatState.sender, target = targetWrapper.targetState.target, msg = tsi.message)

      def send = Callback(ws.send(write(msg)))

      // Update the log, clear the text box
      def updateState = $c.modState(s => {
        s.copy(message = "")
      })

      send >> updateState
    }

    def render(tp: TargetWrapper, tsi: TargetStateInside) = {
      // Can only send if WebSocket is connected and user has entered text
      val sendMsg2Target: Option[Callback] = {
        for (ws <- tp.chatState.ws if tsi.message.nonEmpty)
          yield sendChatMessage2Target(ws, tp, tsi)
      }

      <.div(
        <.label(tp.targetState.target)
        ,
        <.input( // channel input
          ^.width := 250.px,
          ^.onChange ==> onChangeChannelInput,
          ^.value := tsi.message
        )
        ,
        <.button(
          ^.disabled := sendMsg2Target.isEmpty, // Disable button if unable to send
          ^.onClick -->? sendMsg2Target, // --> suffixed by ? because it's for Option[Callback]
          "Send"
        )
      )
    }

  }

  val ChannelChat = ScalaComponent.builder[TargetWrapper]("ChannelChat")
    .initialState(defaultTargetStateInside)
    .renderBackend[ChannelBackend]
    //.componentWillMount(f => { f.modState(_.copy(logLines = f.))})
    .build

  class Backend($: BackendScope[IrcChatProps, ChatState]) {

    def render(props: IrcChatProps, s: ChatState) = {
      val sendJoinMsg2Target: Option[Callback] = {
        for (ws <- s.ws if s.channelJoin.nonEmpty)
          yield sendJoinTargetMessage(ws, props, s)
      }


      <.div(
        <.h3("Type a message and get an echo:"),
        <.div(
          <.label(
            "Username"
          ),
          <.input(
            ^.value := s.sender
          ),
          <.button(
            ^.onClick --> start(props.url, props, s), // --> suffixed by ? because it's for Option[Callback]
            "Connect"),
          <.br,

          <.label(
            "Channel"
          ),
          <.input(
            ^.onChange ==> onChangeInputChannel,
            ^.value := s.channelJoin
          ),

          <.button(
            ^.disabled := sendJoinMsg2Target.isEmpty, // Disable button if unable to send
            ^.onClick -->? sendJoinMsg2Target, // --> suffixed by ? because it's for Option[Callback]
            "Join channel"),
          <.br
          ,
          <.h4("Irc chat channels")
          ,
          <.div(
            ^.minWidth := 500.px,
            ^.minHeight := 300.px,
            ^.border := "1px solid",
            <.div(
              s.targets.map(
                f => {
                  <.div(
                    ChannelChat(TargetWrapper(chatState = s, targetState = f, ircChatProps = props))
                  )
                }
              ).toTagMod

              // print info and messages
              ,
              s.targets.map(
                f => {
                  <.div(
                    <.div(
                      <.h4(f.target),
                      <.div(s"Info for target: ${f.target}"),
                      f.tsi.logLines.map(g => {
                        <.p(
                          <.span(g.sender),
                          ": ",
                          <.span(g.msg)
                        )
                      }).toTagMod
                    )
                  )
                }
              ).toTagMod

            )

          ) // Display log
        )
      )
    }

    def onChangeInputChannel(e: ReactEventFromInput): Callback = {
      val channel: String = e.target.value

      $.modState(_.copy(
        channelJoin = channel
      ))
    }

    def sendJoinTargetMessage(ws: WebSocket, props: IrcChatProps, s: ChatState): Callback = {
      org.scalajs.dom.console.log("sendJoinTargetMessage")
      // send join message to websocket
      val msg: JsMessageJoinChannel = JsMessageJoinChannel(s.sender, s.channelJoin)

      def send = Callback(ws.send(write(msg)))

      // Update the log, clear the text box
      def updateState = $.modState(s => {
        s.copy(channelJoin = "")
      })

      org.scalajs.dom.console.log("sendJoinTargetMessage")
      org.scalajs.dom.console.log(msg.toString)

      send >> updateState
    }

    def start(url: String, props: IrcChatProps, s: ChatState): Callback = {

      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {

        // Get direct access so WebSockets API can modify state directly
        // (for access outside of a normal DOM/React callback).
        // This means that calls like .setState will now return Unit instead of Callback.
        // casting to MountedWithRoot[...] type for syntax support
        val direct: MountedWithRoot[CallbackTo,IrcChatProps,ChatState,IrcChatProps,ChatState] = $.withEffectsImpure.asInstanceOf[MountedWithRoot[CallbackTo,IrcChatProps,ChatState,IrcChatProps,ChatState]]

        // These are message-receiving events from the WebSocket "thread".
        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          direct.modState(_.logLine("Connected."))
        }

        def onmessage(e: MessageEvent): Unit = {
          // Echo message received

          org.scalajs.dom.console.log("jsval")
          org.scalajs.dom.console.log(e.data.toString)

          val incommingMsg = read[JsMessageBase](e.data.toString)

          def addTarget(targets : ListBuffer[TargetProps], newTarget : TargetProps): ListBuffer[TargetProps] = {
            org.scalajs.dom.console.log(s"addTarget newTarget ${newTarget.toString}")
            targets += newTarget
            org.scalajs.dom.console.log(s"addTarget targets ${targets.toString}")
            targets
          }

          incommingMsg match {
            case k : JsMessage => {
              // handle the JsMessage
              org.scalajs.dom.console.log(s"JsMessage ${k.toString}")
              direct.modState(_.logTargetLine(k))
            }
            case JsMessageJoinChannel(sender, target) => {
              // handle the JsMessageJoinChannel
              org.scalajs.dom.console.log("JsMessageJoinChannel")
              direct.modState(f => {
                f.copy(targets = addTarget(f.targets,TargetProps(target = target, password = "", TargetStateInside(Vector.empty[JsMessage], ""))))
              })
            }
            case JsMessageLeaveChannel(sender, target) => {
              // handle the JsMessageLeaveChannel
              org.scalajs.dom.console.log("JsMessageLeaveChannel")
              direct.modState(_.logLine(s"JsMessageLeaveChannel: ${e.data.toString}"))
            }
          }
        }

        def onerror(e: ErrorEvent): Unit = {
          // Display error message
          direct.modState(_.logLine(s"Error: ${e.message}"))
        }

        def onclose(e: CloseEvent): Unit = {
          // Close the connection
          direct.modState(_.copy(ws = None).logLine(s"Closed: ${e.reason}"))
        }

        // Create WebSocket and setup listeners
        val ws: WebSocket = new WebSocket(url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      // Here use attemptTry to catch any exceptions in connect.
      connect.attemptTry.flatMap {
        case Success(ws) => $.modState(_.logLine(s"Connecting to ${url}").copy(ws = Some(ws)))
        case Failure(error) => $.modState(_.logLine(error.toString))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close()))

      def clearWebSocket = $.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
    }
  }

  val WebSocketsApp = ScalaComponent.builder[IrcChatProps]("WebSocketsApp")
    .initialState(ChatState(None, ListBuffer(defaultTargetProps), "", "#TheName2"))
    .renderBackend[Backend]
    // set username (username has to be as chat state atr because it can be changed via /anick command)
    .componentWillMount(f => {
    f.modState(_.copy(sender = f.props.username))
  })
    .componentWillUnmount(_.backend.end)
    .build

}
