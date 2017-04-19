
package utils

import com.google.inject.AbstractModule

class DemoDataModule extends AbstractModule {
  override protected def configure(): Unit = bind(classOf[DemoData]).asEagerSingleton()
}