VERSION=$$(cat VERSION)

default: test

deep_clean: clean
	(rm -fr ./target ; rm -fr ./project/project ; rm -fr ./project/target) || echo "it's clean"

clean:
	sbt clean

fmt:
	sbt soteriaCheckScalaFmtRun

publishLocal:
	 sbt 'set isSnapshot := true' publishLocal

publish: test publish_only

publish_only:
	git tag -a $(VERSION) -m $(VERSION)
	git push origin $(VERSION)

have_right_version:
	cat ./project/soteria.sbt | grep `cat ./VERSION | cut -d "v" -f 2` && \
	echo "Plugin have right version!"

# https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html
test_plugin:
	sbt '; set isSnapshot := true ; scripted'

# https://stackoverflow.com/a/42430476
test_plugin_one:
	sbt "; set isSnapshot := true ; scripted $(TEST_NAME)"

test_coverage_run:
	sbt ";clean; coverage; test; coverageReport"
	open ./target/scala-2.12/sbt-1.0/scoverage-report/index.html

test_coverage:
	sbt ";clean; coverage; test; coverageReport; coveralls"

check_style:
	sbt soteriaCheckScalaFmt

unit_test:
	sbt ";clean; test"

test: deep_clean check_style publishLocal unit_test test_plugin

mutator_test:
	export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -Xmx4G"
	sbt 'set logLevel in Test := Level.Error' 'set parallelExecution in Test := true' 'set soteriaSoftOnCompilerWarning := true' stryker

mutator_open_results:
	open `find ./target/stryker4s* -type f -iname "*index.html"`

mutator_test_run: mutator_test mutator_open_results
