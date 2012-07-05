//MAIN CONFIGURATION IN project/Build.scala

name := "scala-codeshare"

unmanagedSourceDirectories in Compile <++= baseDirectory { base => Seq(base / "src/main") } 

resolvers ++= Seq(
            // other resolvers here
            "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/",
            "ScalaNLP Maven2" at "http://repo.scalanlp.org/repo"
)


libraryDependencies ++= Seq(
   "junit" % "junit" % "4.8" % "test->default"
   )