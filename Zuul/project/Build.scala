import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "Zuul"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.springframework" % "spring-context" % "3.1.1.RELEASE",
      "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.6",
      "commons-collections" % "commons-collections" % "3.2.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
