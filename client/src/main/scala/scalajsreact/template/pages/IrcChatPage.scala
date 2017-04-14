package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom._
import shared.SharedMessages._
import sun.text.normalizer.Utility
import upickle.default.{read, _}

import scala.collection.mutable.ListBuffer
import scala.reflect.runtime
import scala.scalajs.js.JSConverters.genTravConvertible2JSRichGenTrav
import scala.util.control.TailCalls.Call
import scala.util.{Failure, Success}
import scalajsreact.template.components.LeftNav.Style.{&, style, styleF}
import scalajsreact.template.models.IrcChatProps
import scalajsreact.template.pages.IrcChatPage.ChatState
import scalacss.Defaults._
import scalacss.ScalaCssReact._

/**
  * Information base
  * WebSocket communication works as ClientRequest-ServerResponse so all client actions can be authenticated on server side
  * Methods of case classes ChatState and TargetState:
  *   - starts with "call.*" : js call and unpack Option[WebSocket] and run all input checks
  *   - starts with "send.*" : sends WebSocket message
  */
object IrcChatPage {
  //def currentMethodName(): String = Thread.currentThread.getStackTrace()(2).getMethodName

  // should log stack trace of parent method?
  def logThisMethodJs(): Unit = {
    org.scalajs.dom.console.log(Thread.currentThread.getStackTrace().toString)
  }

  // contains live info about channel
  case class TargetState(target: String, password: String, var logLines: ListBuffer[JsMessage], var inputMessage: String) {

    // call on leave button
    def callOnLeaveButton(props: IrcChatProps, s: ChatState): Option[Callback] = {
      for (ws <- s.ws)
        yield sendLeaveTargetMessage(ws, props, s)
    }

    def isSendLeaveButtonDisabled(s: ChatState): Boolean = {
      s.ws.isEmpty || !s.isReady
    }

    def sendLeaveTargetMessage(ws: WebSocket, props: IrcChatProps, s: ChatState): Callback = {
      logThisMethodJs()
      // send join message to websocket
      val msg: JsMessageLeaveChannel = JsMessageLeaveChannel(s.sender, this.target)

      def send = Callback(ws.send(write(msg)))

      //this.channelJoin = ""

      logThisMethodJs()
      org.scalajs.dom.console.log(msg.toString)

      send // >> updateState
    }

    import org.scalajs.dom.ext.KeyCode

    //send Messages to Targets methods - <input
    /* def callOnEnterSendTargetMessage(props: IrcChatProps, s: ChatState)(e: ReactKeyboardEventFromHtml): Callback = {
       if (e.keyCode == KeyCode.Enter) {
       } else {
         Option[Callback[None]]
       }
     }*/

    // this call unpack WebSocket instance from Call and
    def callSendTargetMessage(props: IrcChatProps, s: ChatState): Option[Callback] = {
      for (ws <- s.ws if this.inputMessage.nonEmpty)
        yield sendTargetMessage(ws, props, s)
    }

    // checks if WebSocket instance is connected
    def callSendTargetMessageDisabled(s: ChatState): Boolean = {
      s.ws.isEmpty || this.inputMessage.isEmpty || !s.isReady
    }

    //sends Message to Target using unpacked WebSocket instance
    def sendTargetMessage(ws: WebSocket, props: IrcChatProps, s: ChatState): Callback = {
      // send join message to websocket
      val msg: JsMessage = JsMessage(s.sender, this.target, this.inputMessage)

      def send = Callback(ws.send(write(msg)))

      logThisMethodJs()
      org.scalajs.dom.console.log(msg.toString)

      this.inputMessage = ""

      send // >> updateState
    }

    def callOnChangeTargetInputEnter($: MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState],
                                     props: IrcChatProps, s: ChatState)(e: ReactKeyboardEventFromHtml): Callback = {
      if (e.keyCode == KeyCode.Enter) {
        callSendTargetMessage(props, s).getOrElse(Callback.empty)
      } else {
        Callback.empty
      }
    }

    //js call Targets <input onchange/>
    def callOnChangeTargetInput($: MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState],
                                props: IrcChatProps, s: ChatState)(e: ReactEventFromInput): Callback = {
      val targetInput = e.target.value
      $.modState(state => {
        state.copy(targets = state.setInputMessageAndReturnAllTargets(this, targetInput))
      })

    }

  }

  val defaultTargetStateInside = TargetState("#TheName", "", logLines = ListBuffer.empty[JsMessage], inputMessage = "")

  case class ChatState(ws: Option[WebSocket], var targets: ListBuffer[TargetState], sender: String,
                       var channelJoin: String, /*channelJoinPassword : String,*/ var selectedTarget: Option[TargetState], var ready: Boolean) {

    // if no target view is selected and targets list is not empty then set the first target as selected view
    def setDefaultSelectedTargetIfNone(): Unit = {
      if (targets.nonEmpty && this.selectedTarget.isEmpty) {
        this.selectedTarget = Some(targets(0))
      }
    }


    def setInputMessageAndReturnAllTargets(target: TargetState, inputMessage: String): ListBuffer[TargetState] = {
      // logThisMethodJs()
      targets.filter(_ == target).foreach(_.inputMessage = inputMessage)
      targets
    }

    // Create a new state with a line added to the log
    def logLine(line: String): ChatState = {
      org.scalajs.dom.console.log("ChatState:logLine: " + this)
      this
    }

    def logTargetLine(msg: JsMessage): ChatState = {
      logThisMethodJs()
      var state: ChatState = this
      if (targets.isEmpty) {}

      targets.filter(_.target == msg.target).foreach(f => {
        f.logLines = f.logLines += msg
      })

      state = copy(targets = targets)

      org.scalajs.dom.console.log(state.toString)

      state
    }

    def isReady(): Boolean = {
      this.ready
    }

    def leaveTarget(target: String): ChatState = {
      // TODO change so just 1 specific target would be removed and not all ow them with same name
      this.targets = this.targets.filterNot(_.target == target)
      this
    }

    def sendStartIrcBotRequest(): ChatState = {
      val request: JsMessageStarBot = JsMessageStarBot(this.sender, Array[String]("#TheName", "#TheName2"))
      this.logLine("Connected.")

      this.callJsMessageAndReturnCallback(request)
      this
    }

    def callJsMessageAndReturnCallback[T <: JsMessageBase](msg: T): Option[Callback] = {
      for (ws <- this.ws)
        yield sendJsMessageAndReturnCallback(ws, msg)
    }

    def sendJsMessageAndReturnCallback[T <: JsMessageBase](ws: WebSocket, msg: T): Callback = {
      logThisMethodJs()
      // send join message to websocket

      def send = Callback(ws.send(write[JsMessageBase](msg)))

      org.scalajs.dom.console.log(msg.toString)

      send // >> updateState
    }

    def callSendJoinTargetMessage(props: IrcChatProps): Option[Callback] = {
      for (ws <- this.ws if this.channelJoin.nonEmpty)
        yield sendJoinTargetMessage(ws, props, this)
    }

    def isSendJoinTargetMessageDisabled(): Boolean = {
      this.ws.isEmpty || this.channelJoin.isEmpty || !this.isReady
    }

    def sendJoinTargetMessage(ws: WebSocket, props: IrcChatProps, s: ChatState): Callback = {
      logThisMethodJs()
      // send join message to websocket
      val msg: JsMessageJoinChannel = JsMessageJoinChannel(s.sender, s.channelJoin)

      def send = Callback(ws.send(write(msg)))

      this.channelJoin = ""

      logThisMethodJs()
      org.scalajs.dom.console.log(msg.toString)

      send // >> updateState
    }

    def callOnChangeInputJoinChannel($: MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState])(e: ReactEventFromInput): Callback = {
      val channelInput = e.target.value
      $.modState(state => {
        state.copy(channelJoin = channelInput)
      })
    }

  }

  object TargetStyle extends StyleSheet.Inline {

    import dsl._

    val chatContainer = style(minHeight(600.px), padding(15.px))

    val nav =
      style(display.block, float.left, width(200.px), borderRight :=! "1px solid rgb(223, 220, 220)")

    val targetContent = style(marginTop(30.px),padding(30.px), display.inlineBlock, width(700.px))

    val targetListContainer = style(display.flex,
      flexDirection.column,
      listStyle := "none",
      padding.`0`,
      width(150.px),
      float.left
    )

    val targetHidden = styleF.bool { selected =>
      styleS(
        mixinIf(selected)(display.none)
      )
    }


    val targetListItem = styleF.bool { selected =>
      styleS(
        lineHeight(48.px),
        padding :=! "0 10px",
        cursor.pointer,
        textDecoration := "none",
        mixinIf(selected)(color.red, fontWeight._500)
      )
    }
  }


  class Backend($: BackendScope[IrcChatProps, ChatState]) {

    def render(props: IrcChatProps, s: ChatState) = {
      // TODO check selected target
      // set default selected target
      s.setDefaultSelectedTargetIfNone()
      org.scalajs.dom.console.log("selected")
      org.scalajs.dom.console.log(s.selectedTarget.isEmpty)
      org.scalajs.dom.console.log(s.selectedTarget.getOrElse(this).toString)
      org.scalajs.dom.console.log(s.selectedTarget.getOrElse(this).equals(s.selectedTarget.getOrElse(false)))


      <.div(
        TargetStyle.chatContainer,
        <.h3("Type a message and get an echo:"),
        <.div(
          <.label(
            "Username"
          ),
          <.input(
            ^.value := s.sender
          ),
          <.button(
            ^.onClick --> start(props.url, props, s, None), // --> suffixed by ? because it's for Option[Callback]
            "Connect"),
          <.br,
          <.label(
            "Channel"
          ),
          <.input(
            // ^.onChange ==> targetState.callOnChangeTargetInput($, props, targetState),
            ^.onChange ==> s.callOnChangeInputJoinChannel($),
            ^.value := s.channelJoin
          ),
          <.button(
            ^.disabled := s.isSendJoinTargetMessageDisabled(), // Disable button if unable to send
            ^.onClick -->? s.callSendJoinTargetMessage(props), // --> suffixed by ? because it's for Option[Callback]
            "Join channel")
        ),
        <.ul(TargetStyle.targetListContainer,
          // create list menu for selecting targets view
          <.li(
            TargetStyle.targetListItem(false),
            <.h4("Irc chat channels")
          ),
          s.targets.map(
            targetState => {
              <.li(
                TargetStyle.targetListItem(targetState.equals(s.selectedTarget.getOrElse(true))),
                <.button(
                  ^.onClick --> callSetSelectedTarget(targetState),
                  targetState.target
                )
              )
            }).toTagMod
        ),
        <.div(
          TargetStyle.targetContent,
          ^.minWidth := 500.px,
          ^.minHeight := 300.px,
          ^.border := "1px solid",
          <.div(
            s.targets.map(
              targetState => {
                <.div(
                  TargetStyle.targetHidden(!targetState.equals(s.selectedTarget.getOrElse(true))),
                  <.div(
                    <.label(targetState.target),
                    <.input( // channel input
                      ^.width := 250.px,
                      ^.onChange ==> targetState.callOnChangeTargetInput($, props, s),
                      ^.onKeyDown ==> targetState.callOnChangeTargetInputEnter($, props, s),
                      ^.value := targetState.inputMessage
                    ),
                    <.button(
                      ^.disabled := targetState.callSendTargetMessageDisabled(s), // Disable button if unable to send
                      ^.onSubmit -->? targetState.callSendTargetMessage(props, s), // --> suffixed by ? because it's for Option[Callback]
                      ^.onClick -->? targetState.callSendTargetMessage(props, s),
                      "Send"
                    ),
                    <.button(
                      ^.disabled := targetState.isSendLeaveButtonDisabled(s),
                      ^.onClick -->? s.callSendJoinTargetMessage(props),
                      ^.onClick -->? targetState.callOnLeaveButton(props, s),
                      "Leave"
                    )
                  ),
                  <.h4(targetState.target),
                  <.div(s"Info for target: ${targetState.target}"),
                  targetState.logLines.map(g => {
                    <.p(
                      <.span(g.sender),
                      ": ",
                      <.span(g.msg)
                    )
                  }).toTagMod
                )
              }
            ).toTagMod
            // print info and messages
          )
        ) // Display log
      )
    }

    // js call for setting selected target view, should change state and render again?
    def callSetSelectedTarget(ts: TargetState): Callback = {
      logThisMethodJs()
      org.scalajs.dom.console.log(s" newTarget ${ts.toString}")
      $.modState(state => {
        state.copy(selectedTarget = Some(ts))
      })
    }

    // to get $ loaded with Everything
    def getDirect(): MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState] = {
      $.withEffectsImpure.asInstanceOf[MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState]]
    }

    def start(url: String, props: IrcChatProps, s: ChatState, optWs: Option[WebSocket]): Callback = {

      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {

        // Get direct access so WebSockets API can modify state directly
        // (for access outside of a normal DOM/React callback).
        // This means that calls like .setState will now return Unit instead of Callback.
        // casting to MountedWithRoot[...] type for syntax support
        val direct: MountedWithRoot[CallbackTo, IrcChatProps, ChatState, IrcChatProps, ChatState] = this.getDirect()

        // These are message-receiving events from the WebSocket "thread".
        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          //TODO message containing default information

          direct.modState(_.sendStartIrcBotRequest())
          direct.modState(_.logLine("Connected."))
        }

        def onmessage(e: MessageEvent): Unit = {
          // Echo message received

          org.scalajs.dom.console.log("jsval")
          org.scalajs.dom.console.log(e.data.toString)

          val incommingMsg = read[JsMessageBase](e.data.toString)

          def addTarget(targets: ListBuffer[TargetState], newTarget: TargetState): ListBuffer[TargetState] = {
            logThisMethodJs()
            org.scalajs.dom.console.log(s" newTarget ${newTarget.toString}")
            targets += newTarget
            org.scalajs.dom.console.log(s" targets ${targets.toString}")
            targets
          }

          incommingMsg match {
            case k: JsMessage => {
              // handle the JsMessage
              org.scalajs.dom.console.log(s"JsMessage ${k.toString}")
              direct.modState(_.logTargetLine(k))
            }
            case JsMessageJoinChannel(sender, target) => {
              // handle the JsMessageJoinChannel
              org.scalajs.dom.console.log("JsMessageJoinChannel")
              direct.modState(f => {
                f.copy(targets = addTarget(f.targets, TargetState(target = target, password = "", ListBuffer.empty[JsMessage], "")))
              })
            }
            case JsMessageLeaveChannel(sender, target) => {
              // handle the JsMessageLeaveChannel
              org.scalajs.dom.console.log("JsMessageLeaveChannel")

              direct.modState(_.leaveTarget(target))
            }
            case JsMessageIrcBotReady() => {
              direct.modState(_.copy(ready = true))
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
    .initialState(ChatState(None, ListBuffer(defaultTargetStateInside), "", "#TheName2", None, false))
    .renderBackend[Backend]
    // set username (username has to be as chat state atr because it can be changed via /anick command)
    .componentWillMount(f => {
    f.modState(_.copy(sender = f.props.username))
  })
    .componentWillUnmount(_.backend.end)
    .build

}
