package shared


object Shared {
 private var data: String = "empty"

 def setData(d: String) : Unit = data = d
 def getData : String = data
}
