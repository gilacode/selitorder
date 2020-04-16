resolvers += Resolver.url("play-messagescompiler", url("https://github.com/tegonal/tegonal-mvn/raw/master/releases/"))

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.17")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "4.1.3")
addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.2.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")