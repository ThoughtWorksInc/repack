enablePlugins(JavaServerAppPackaging)

enablePlugins(DockerPlugin)

enablePlugins(SbtWeb)

enablePlugins(SbtSassify)

enablePlugins(WebScalaJSBundlerPlugin)

enablePlugins(BuildInfoPlugin)

pipelineStages in Assets += scalaJSPipeline

// Compile-time SASS files, used by SbtSassify only.
libraryDependencies += "org.webjars" % "bootstrap" % "4.3.1" % Provided

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.21"

libraryDependencies += "com.github.alexarchambault" %% "case-app" % "2.0.0-M7"

libraryDependencies += "io.github.lhotari" %% "akka-http-health" % "1.0.8"

libraryDependencies += "org.webjars" % "webjars-locator" % "0.36"

libraryDependencies += "com.thoughtworks.akka-http-webjars" %% "akka-http-webjars" % "1.0.0+95-97299c01"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "5.3.0.201903130848-r"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-bangnotation" % "1.1.1+23-b7dac581")

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-reseteverywhere" % "1.1.1+23-b7dac581")

libraryDependencies += "com.thoughtworks.dsl" %% "domains-akka-http" % "0.0.0+116-c5d8c151"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-await" % "1.1.1+23-b7dac581"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-catch" % "1.1.1+23-b7dac581"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-return" % "1.1.1+23-b7dac581"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-nullsafe" % "1.1.1+23-b7dac581"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-nonesafe" % "1.1.1+23-b7dac581"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-using" % "1.1.1+23-b7dac581"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

fork := true

ThisBuild / dynverSeparator := "-"

dockerBaseImage := "openjdk:11.0.2"

buildInfoPackage := "com.thoughtworks.modularizer.server"
