import ReleaseTransformations._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import sbtrelease.ReleasePlugin

lazy val quill = 
  (project in file("."))
    .settings(tutSettings ++ commonSettings)
    .settings(`tut-settings`:_*)
    .dependsOn(`quill-core`, `quill-sql`, `quill-jdbc`, `quill-finagle-mysql`, `quill-async`, `quill-cassandra`)
    .aggregate(`quill-core`, `quill-sql`, `quill-jdbc`, `quill-finagle-mysql`, `quill-async`, `quill-cassandra`)

lazy val `quill-core` = 
  (project in file("quill-core"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .settings(libraryDependencies ++= Seq(
      "com.typesafe"               %  "config"        % "1.3.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
      "org.scala-lang"             %  "scala-reflect" % scalaVersion.value
    ))

lazy val `quill-sql` = 
  (project in file("quill-sql"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .dependsOn(`quill-core` % "compile->compile;test->test")

lazy val `quill-jdbc` = 
  (project in file("quill-jdbc"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.zaxxer"     % "HikariCP"             % "2.4.6",
        "mysql"          % "mysql-connector-java" % "5.1.38"  % "test",
        "com.h2database" % "h2"                   % "1.4.192" % "test",
        "org.postgresql" % "postgresql"           % "9.4.1208" % "test"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-sql` % "compile->compile;test->test")

lazy val `quill-finagle-mysql` = 
  (project in file("quill-finagle-mysql"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-mysql" % "6.35.0"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-sql` % "compile->compile;test->test")

lazy val `quill-async` = 
  (project in file("quill-async"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.github.mauricio" %% "db-async-common"  % "0.2.20-DRIP-4",
        "com.github.mauricio" %% "mysql-async"      % "0.2.20-DRIP-4",
        "com.github.mauricio" %% "postgresql-async" % "0.2.20-DRIP-4"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-sql` % "compile->compile;test->test")

lazy val `quill-cassandra` = 
  (project in file("quill-cassandra"))
    .settings(commonSettings: _*)
    .settings(mimaSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.datastax.cassandra" %  "cassandra-driver-core" % "3.0.2",
        "org.monifu"             %% "monifu"                % "1.2"
      ),
      parallelExecution in Test := false
    )
    .dependsOn(`quill-core` % "compile->compile;test->test")

lazy val `tut-sources` = Seq(
  "CASSANDRA.md",
  "README.md"
)

lazy val `tut-settings` = Seq(
  tutScalacOptions := Seq(),
  tutSourceDirectory := baseDirectory.value / "target" / "tut",
  tutNameFilter := `tut-sources`.map(_.replaceAll("""\.""", """\.""")).mkString("(", "|", ")").r,
  sourceGenerators in Compile <+= Def.task {
    `tut-sources`.foreach { name =>
      val source = baseDirectory.value / name
      val file = baseDirectory.value / "target" / "tut" / name
      val str = IO.read(source).replace("```scala", "```tut")
      IO.write(file, str)
    }
    Seq()
  }
)

lazy val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
  previousArtifact := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor <= 11 =>
        Some(organization.value % s"${name.value}_${scalaBinaryVersion.value}" % "0.5.0")
      case _ =>
        None
    }
  }
)

lazy val commonSettings = ReleasePlugin.extraReleaseCommands ++ Seq(
  organization := "io.getquill",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalamacros" %% "resetallattrs"  % "1.0.0",
    "com.lihaoyi"     %% "sourcecode"     % "0.1.2",
    "org.scalatest"   %% "scalatest"      % "2.2.6" % "test",
    "ch.qos.logback"  % "logback-classic" % "1.1.7" % "test",
    "com.google.code.findbugs" % "jsr305" % "3.0.1" % "provided" // just to avoid warnings during compilation
  ),
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  unmanagedClasspath in Test ++= Seq(
    baseDirectory.value / "src" / "test" / "resources"
  ),
  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",       
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",   
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"
  ),
  fork in Test := true,
  scoverage.ScoverageKeys.coverageMinimum := 96,
  scoverage.ScoverageKeys.coverageFailOnMinimum := false,
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(FormatXml, true)
    .setPreference(PreserveSpaceBeforeArguments, false)
    .setPreference(DoubleIndentClassDeclaration, false)
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(PreserveDanglingCloseParenthesis, false)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentLocalDefs, false)
    .setPreference(SpacesWithinPatternBinders, true)
    .setPreference(SpacesAroundMultiImports, true),
  EclipseKeys.eclipseOutput := Some("bin")
)
