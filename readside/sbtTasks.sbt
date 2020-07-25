

lazy val readside = taskKey[Unit]("Start readside")

readside := (runMain in Compile ).toTask(" readside.Main").value
