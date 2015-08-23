// studyplay2

val appName = "studyplay2"

val appVersion = "1.0.0"

val baseSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-unused-import",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions"
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
  "com.typesafe.play"             %% "play-slick"                          % "1.0.1",
  "com.typesafe.play"             %% "play-slick-evolutions"               % "1.0.1",
  "com.sksamuel.elastic4s"        %% "elastic4s"                           % "1.5.13",
  "org.scalatest"                 %% "scalatest"                           % "2.2.4"                 % "test",
  "org.scalatestplus"             %% "play"                                % "1.4.0-M3"              % "test",
  "org.codelibs"                   % "elasticsearch-cluster-runner"        % "1.5.0.1"               % "test"
)


import com.scalapenos.sbt.prompt._
import SbtPrompt.autoImport._

val customPromptTheme = PromptTheme(
  List(
    text("[", fg(white)),
    currentProject(fg(cyan)),
    text("] ", fg(white)),
    gitBranch(clean = fg(green), dirty = fg(red)),
    text(" $ ", fg(yellow))
  )
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
).settings(
  promptTheme := customPromptTheme,
  shellPrompt := (implicit state => promptTheme.value.render(state))
).enablePlugins(RpmPlugin).settings(
  maintainer := "Tomofumi Tanaka <tanacasino@gmail.com>",
  packageSummary := "My play application summary.",
  packageDescription := "My play application description.",
  rpmVendor := "tanacasino.github.com",
  rpmUrl := Some("https://github.com/tanacasino/studyplay2"),
  rpmLicense := Some("Apache 2 License")
)

