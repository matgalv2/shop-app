addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.twilio" % "sbt-guardrail" % "0.64.3")
addSbtPlugin("io.spray"   % "sbt-revolver"  % "0.9.1")

// Better syntax for dealing with partially-applied types
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
// Better semantics for for comprehensions
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
dependencyOverrides ++= Seq("org.scala-lang.modules" % "scala-xml_2.12" % "2.1.0")