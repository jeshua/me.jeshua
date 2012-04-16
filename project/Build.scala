import sbt._
import Keys._

object BuildSettings {
  val buildScalaVersion = "2.9.1"
  val buildSettings = Defaults.defaultSettings ++ Seq (
    scalaVersion := buildScalaVersion,
    traceLevel := 0,
    testOptions in Test += Tests.Argument("-oD"),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    unmanagedJars in Compile <++= baseDirectory.map(bd => (bd / "lib" / "libs_amd64" ***) get)
  )
}

//----------------------------------------------------------------------
object Resolvers {
  val nlprepo = "ScalaNLP Maven2" at "http://repo.scalanlp.org/repo"
  val scalatoolsrepo = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
}

object Dependencies {
  val scalaio = "com.github.scala-incubator.io" % "scala-io-core_2.9.1" % "0.2.0"
  val xmlgraphics = "org.apache.xmlgraphics" % "xmlgraphics-commons" % "1.4"
  val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.5"
  val jcommon = "jfree" % "jcommon" % "1.0.16"
  val jfree = "jfree" % "jfreechart" % "1.0.13"
}

//----------------------------------------------------------------------
object srltkBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  // Sub-project specific dependencies
  val commonDeps = Seq (
    scalaio,proguard,xmlgraphics,jcommon,jfree
  )

  lazy val srltk = Project (
    "scala-code",
    file ("."),
    settings = buildSettings ++ Seq (resolvers := Seq(nlprepo,scalatoolsrepo),
                                     libraryDependencies ++= commonDeps)
  )
}
