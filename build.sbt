name := "google_excel_web_editor"
 
version := "1.0" 
      
lazy val `google_excel_editor` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

Test / unmanagedResourceDirectories += (baseDirectory.value / "target/web/public/test")
