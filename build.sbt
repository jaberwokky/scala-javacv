name := "scala-javacv"

version := "1.0"

scalaVersion := "2.12.1"

// https://mvnrepository.com/artifact/org.bytedeco.javacpp-presets/opencv
libraryDependencies ++= Seq("org.bytedeco.javacpp-presets" % "opencv" % "3.2.0-1.3",
  "org.bytedeco.javacpp-presets" % "opencv-platform" % "3.2.0-1.3",
  "org.bytedeco" % "javacv" % "1.3.2",
  "org.bytedeco" % "javacv-platform" % "1.3.2",
  "org.bytedeco" % "javacpp" % "1.3.2")