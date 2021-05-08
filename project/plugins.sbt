logLevel := Level.Warn

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.5.1")
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.12")
