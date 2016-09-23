// studyplay2

val appName = "studyplay2"

val appVersion = "1.0.0"

val baseSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    //"-Ywarn-unused-import",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions"
  ),
  javacOptions in compile ++= Seq(
    "-encoding", "UTF-8",
    "-source", "1.8",
    "-target", "1.8"
  )
)

val appDependencies = Seq(
  // Play libs
  cache,
  ws,

  // Play Thirdparty libs
  "com.typesafe.play"             %% "play-slick"                          % "2.0.2",
  "com.typesafe.play"             %% "play-slick-evolutions"               % "2.0.2",
  "jp.t2v"                        %% "play2-auth"                          % "0.14.2",
  "com.github.mumoshu"            %% "play2-memcached-play24"              % "0.7.0",

  // Others
  "com.sksamuel.elastic4s"        %% "elastic4s"                           % "1.5.13",
  "mysql"                          % "mysql-connector-java"                % "5.1.36",
  "com.h2database"                 % "h2"                                  % "1.4.188",
  "org.mindrot"                    % "jbcrypt"                             % "0.3m",
  "org.scalaz"                    %% "scalaz-core"                         % "7.1.5",


  // Test
  "org.scalatestplus.play"        %% "scalatestplus-play"                  % "1.5.0"                 % "test",

  "org.codelibs"                   % "elasticsearch-cluster-runner"        % "1.5.0.1"               % "test",
  "jp.t2v"                        %% "play2-auth-test"                     % "0.14.2"                % "test"
)


// scalariform
import scalariform.formatter.preferences._

scalariformSettings


// Slick/Playのgenerate したscalaファイルを除外する
excludeFilter in scalariformFormat := new SimpleFileFilter(target => {
  val baseDirectory = file(".").getCanonicalPath
  lazy val relativePath = target.getCanonicalPath.stripPrefix(baseDirectory)
  target.getName == "Tables.scala" ||
    relativePath.startsWith("/target/scala-2.11/routes")
})

scalariformPreferences := scalariformPreferences.value
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  .setPreference(DoubleIndentClassDeclaration, false)


// prompt
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


import play.sbt.PlayImport.PlayKeys._

// Main
lazy val root = Project(
  appName,
  file(".")
).enablePlugins(PlayScala).settings(
  baseSettings: _*
).settings(
  version := appVersion,
  playDefaultPort := 9000
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
).settings(
  maintainer in Docker := "Tomofumi Tanaka <tanacasino@gmail.com>",
  dockerExposedPorts in Docker := Seq(9000)
)



/**
 * Slick Codegen Task
 */
val slickCodeGen = TaskKey[Unit]("slick-codegen", "Generate Slick Code!!!")

slickCodeGen := {
  println("Generate Slick Code Task")

  import com.typesafe.config.ConfigFactory
  import collection.JavaConverters._
  import java.nio.file.Paths

  import slick.codegen.SourceCodeGenerator

  ConfigFactory.parseFileAnySyntax(Paths.get("conf", "application.conf").toFile)
               .getConfig("slick.dbs")
               .root.entrySet.asScala.map { c =>
    val config = c.getValue.atPath(c.getKey).getConfig(c.getKey)

    val name = c.getKey
    val driver = config.getString("driver").stripSuffix("$")
    val dbDriver = config.getString("db.driver")
    val dbUrl = config.getString("db.url")
    val dbUser = config.getString("db.user")
    val dbPassword =config.getString("db.password")
    val outDir = config.getString("outDir")
    val outPackage = config.getString("outPackage")

    println(s"Generating slick code for $name")
    println(s"driver: $driver")
    println(s"dbDriver: $dbDriver")
    println(s"dbUrl: $dbUrl")
    println(s"outDir: $outDir")
    println(s"outPackage: $outPackage")
    SourceCodeGenerator.main(
      Array(
        driver,
        dbDriver,
        dbUrl,
        outDir,
        outPackage,
        dbUser,
        dbPassword
      )
    )
    println(s"Generated slick code for $name\n")
  }

}

slickCodeGen <<= slickCodeGen

