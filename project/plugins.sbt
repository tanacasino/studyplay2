// Playframework
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.16")

// Scalariform
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// Rich Prompt
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "1.0.2")

// For Slick Codegen
libraryDependencies ++= Seq(
  "mysql"                         % "mysql-connector-java"  % "5.1.36",
  "com.typesafe.slick"            %% "slick-codegen"        % "3.1.1"
)

