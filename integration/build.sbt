name := "integration"

libraryDependencies += ("com.github.tmtsoftware.esw"                      %% "esw-testkit"     % "4d97ed9")
libraryDependencies += ("com.github.tmtsoftware.esw-observing-simulation" %% "iris-irisdeploy" % "eab18b4")
libraryDependencies += ("com.github.tmtsoftware.esw-observing-simulation" %% "wfos-wfosdeploy" % "eab18b4")
libraryDependencies += ("com.github.tmtsoftware"                          %% "rtm"             % "b7997a9")
scalafmtConfig                                                            := file("../.scalafmt.conf")
