package utils

import play.api.{Configuration, Environment, Logger}
import play.api.inject.{Binding, Module}
import securesocial.core.RuntimeEnvironment
import service.MyEnvironment

class DemoModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Logger.debug(s"msg: bindings")
    /*
    Here i need to start ircbot if set so in application.conf
    */

    Seq(
      bind[RuntimeEnvironment].to[MyEnvironment]
    )
  }
}