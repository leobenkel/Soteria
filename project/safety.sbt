// https://github.com/leobenkel/soteria
// For ./publishForSelfUse.sh
sys.props.get("without_self_use") match {
  case Some(_) => addSbtPlugin("com.leobenkel" % "soteria" % "0.4.9")
  case None    =>
    val version = IO.readLines(new File("VERSION")).head.drop(1)
    addSbtPlugin("com.leobenkel" % "soteria" % version)
}
