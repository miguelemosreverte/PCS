import Settings._
import sbt.Keys.scalaVersion

lazy val commonSettings = Seq(
  organization in ThisBuild := "wetekio",
  version := "1.0",
  scalaVersion := Dependencies.scalaVersion,
)



lazy val global = project
  .in(file("."))
  .settings(
    name := "Copernico"
  )
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(mainSettings)
  .settings(testSettings)
  .settings(scalaFmtSettings)
  .settings(testCoverageSettings)
  .settings(CommandAliases.aliases)
  .enablePlugins(ScoverageSbtPlugin)
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .enablePlugins(Scripts)
  .aggregate(
    common,
    pcs,
    readside,
    it
  ) configs(FunTest) settings( inConfig(FunTest)(Defaults.testTasks) : _*)


lazy val FunTest = config("fun") extend(Test)

def funTestFilter(name: String): Boolean = ((name endsWith "E2E") || (name endsWith "IntegrationTest"))
def unitTestFilter(name: String): Boolean = ((name endsWith "Spec") && !funTestFilter(name))


testOptions in FunTest := Seq(Tests.Filter(funTestFilter))

testOptions in Test := Seq(Tests.Filter(unitTestFilter))

lazy val globalResources = file("resources")

lazy val common = (project in file("./common"))

lazy val pcs = project
  .settings(Seq(
    Test / parallelExecution := false,
    unmanagedResourceDirectories in Compile += globalResources
  ))
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(
    name := "pcs",
    assemblySettings
  )
  .dependsOn(
    common % "compile->compile;test->test"
  )
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .settings(
    mainClass := Some("Main")
  )
  .settings(
    dockerBaseImage := "openjdk:8",
    dockerUsername := Some("pcs"),
    dockerEntrypoint := Seq("/opt/docker/bin/pcs"),
    dockerExposedPorts := Seq(
      2551, 2552, 2553, 8081, 8083, 8084, 8558
    )
  )
  .settings(
    mainClass in (Compile, run) := Some("Main")
  )




lazy val readside = project
  .settings(Seq(
    Test / parallelExecution := false,
    unmanagedResourceDirectories in Compile += globalResources
  ))
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(
    name := "readside",
    assemblySettings
  )
  .dependsOn(
    pcs % "compile->compile;test->test"
  )
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
    .settings(
      mainClass := Some("readside.Main")
    )
    .settings(
      dockerBaseImage := "openjdk:8",
      dockerUsername := Some("readside"),
      dockerEntrypoint := Seq("/opt/docker/bin/readside"),
      dockerExposedPorts := Seq(
        2554, 8559, 8081
      )
    )



lazy val it = project
  .settings(Seq(
    Test / parallelExecution := false
  ))
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(
    name := "it"
  )
  .dependsOn(
    common % "compile->compile;test->test",
    readside % "compile->compile;test->test",
  )



