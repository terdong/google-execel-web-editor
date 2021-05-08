name := "google_excel_web_editor"

version := "1.0"

lazy val `google_excel_editor` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(ehcache, ws, guice)

libraryDependencies += "com.google.apis" % "google-api-services-sheets" % "v4-rev20210322-1.31.0"
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.31.4"
libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.31.5"
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test",
  "org.scalatest" %% "scalatest" % "3.2.7" % "test"
)

Test / unmanagedResourceDirectories += (baseDirectory.value / "target/web/public/test")
