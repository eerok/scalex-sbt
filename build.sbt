sbtPlugin := true

organization := "org.scalex"

name := "sbt_plugin"

version := "1.1"

scalaVersion := "2.9.3"

crossScalaVersions := Seq("2.9.0", "2.9.1", "2.9.2", "2.10.0", "2.10.1", "2.10.2")

scalacOptions ++= Seq("-deprecation", "-unchecked")

offline := false

publishTo := Some(Resolver.sftp(
  "iliaz",
  "scala.iliaz.com"
) as ("scala_iliaz_com", Path.userHome / ".ssh" / "id_rsa"))
