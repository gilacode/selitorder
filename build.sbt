name := """bukaklist"""
organization := "com.azaman"

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.4"

resolvers += "Jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  evolutions,
  guice,
  javaWs,
  ehcache,
  "com.google.inject.extensions" % "guice-multibindings" % "4.1.0",
  "mysql" % "mysql-connector-java" % "6.0.5",
  "com.h2database" % "h2" % "1.4.197",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "com.squareup.okio" % "okio" % "1.0.0",
  "commons-io" % "commons-io" % "2.6",
  "commons-dbutils" % "commons-dbutils" % "1.6",
  "org.apache.commons" % "commons-dbcp2" % "2.1",
  "org.apache.commons" % "commons-collections4" % "4.1",
  "com.opencsv" % "opencsv" % "4.0",
  "it.innove" % "play2-pdf" % "1.8.2",
  "com.github.kenglxn.QRGen" % "javase" % "2.6.0",
  "org.springframework.security" % "spring-security-jwt" % "1.0.9.RELEASE",
  "org.springframework.security" % "spring-security-crypto" % "5.0.6.RELEASE",
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "org.julienrf" %% "play-jsmessages" % "3.0.0",
  "com.mohiva" %% "play-html-compressor" % "0.7.1",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "me.gosimple" % "nbvcxz" % "1.3.1"
)

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

javaOptions ++= Seq("-Xmx2g", "-Xms1g", "-Djsse.enableCBCProtection=false")

playEbeanModels in Compile := Seq(
  "com.avicenna.config.dbcfg.*",
  "com.avicenna.job.*",
  "com.avicenna.security.*",
  "com.avicenna.logger.dblog.*",
  "com.avicenna.email.*",
  "com.avicenna.uqcode.*",
  "com.avicenna.nav.*",
  "com.avicenna.file.*",
  "com.avicenna.audit.*",
  "com.avicenna.notification.*",
  "com.avicenna.apiclient.*")

PlayKeys.devSettings := Seq("play.server.http.port" -> "9004")

mappings in (Compile, packageBin) ~= { _.filter(!_._1.getName.equals("application.conf")) }

resolvers += "Artifactory" at "http://maven.flexhis.com/artifactory/internal-repo/"
publishTo := Some("Artifactory Realm" at "http://maven.flexhis.com/artifactory/internal-repo;build.timestamp=" + new java.util.Date().getTime)
credentials += Credentials("Artifactory Realm", "maven.flexhis.com", "admin", "AP22iUwWFkf9NywvZWJrj6HfuAN3kxLjbLPof1")
