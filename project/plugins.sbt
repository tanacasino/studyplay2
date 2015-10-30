addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("com.scalapenos" % "sbt-prompt" % "0.2.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")


// For Slick Codegen
libraryDependencies ++= Seq(
  "mysql"                         % "mysql-connector-java"  % "5.1.36",
  "com.typesafe.slick"            %% "slick-codegen"        % "3.1.0"
)

