import sbt._, Keys._

object ScalexSbtPlugin extends Plugin {

  val newTask = TaskKey[Unit]("new-task")
  val newSetting = SettingKey[String]("new-setting")

  // a group of settings ready to be added to a Project
  // to automatically add them, do
  val newSettings = Seq(
    newSetting := "test",
    newTask <<= newSetting map { str ⇒ println(str) }
  )
}
