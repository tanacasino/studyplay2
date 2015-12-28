addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")

// Scalariform
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// Rich Prompt
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "0.2.1")


// For Slick Codegen
libraryDependencies ++= Seq(
  "mysql"                         % "mysql-connector-java"  % "5.1.36",
  "com.typesafe.slick"            %% "slick-codegen"        % "3.1.1"
)

