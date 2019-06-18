VERSION=$$(cat VERSION)

default: test

deep_clean: clean
	(rm -fr ./target ; rm -fr ./project/project ; rm -fr ./project/target) || echo "it's clean"

clean:
	sbt clean

fmt:
	sbt safetyCheckScalaFmtRun || echo ">> Need to add the plugin to itself !"

publishLocal:
	 sbt 'set isSnapshot := true' publishLocal

publish: test publish_only

publish_only:
	git tag -a $(VERSION) -m $(VERSION)
	git push origin $(VERSION)

have_right_version:
	cat ./project/safety.sbt | grep `cat ./VERSION`

# https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html
test_plugin: publishLocal
	sbt 'set isSnapshot := true' scripted

test_coverage_run:
	sbt clean coverage test coverageReport
	open ./target/scala-2.12/sbt-1.0/scoverage-report/index.html

test_coverage:
	sbt clean coverage test

test_coverage_report:
	sbt coverageReport && sbt coverageAggregate

check_style:
	sbt safetyCheckScalaFmt || echo ">> Need to add the plugin to itself !"

unit_test:
	sbt clean test

test: deep_clean publishLocal check_style unit_test test_plugin

mutator_test:
	export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=4G -Xmx4G"
	sbt stryker

mutator_open_results:
	open `find ./target/stryker4s* -type f -iname "*index.html"`

mutator_test_run: mutator_test mutator_open_results
