// studyplay2

val appName = "studyplay2"

val appVersion = "1.0.0"

val baseSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= (
    "-feature" ::
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-Ywarn-dead-code" ::
    "-Ywarn-unused-import" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ),
  javacOptions in compile ++= Seq(
   "-encoding", "UTF-8",
    "-source", "1.8",
    "-target", "1.8"
  ),
  resolvers ++= Seq(Opts.resolver.sonatypeReleases)
)

val appDependencies = Seq(
  cache,
  ws,
  "mysql"                          % "mysql-connector-java"                % "5.1.26",
  "com.h2database"                 % "h2"                                  % "1.4.187",
  "com.typesafe.play"             %% "play-slick"                          % "1.0.0",
  "com.sksamuel.elastic4s"        %% "elastic4s"                           % "1.5.17",
  "org.scalatest"                 %% "scalatest"                           % "2.1.6"                 % "test"
)

lazy val root = Project(
  appName,
  file(".")
).enablePlugins(PlayScala).settings(
  baseSettings: _*
).settings(
  version := appVersion
).settings(
  libraryDependencies ++= appDependencies
)

