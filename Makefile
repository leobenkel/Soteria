
default: test

deep_clean:
	(rm -fr ./target ; rm -fr ./project/project ; rm -fr ./project/target) || echo "it's clean"

clean:
	sbt clean

fmt:
	sbt safetyCheckScalaFmtRun || echo "Need to add the plugin to itself"

publishLocal: fmt
	 sbt 'set isSnapshot := true' publishLocal && sbt 'set isSnapshot := true' publishLocal

publish: have_right_version test
	sbt publish

have_right_version:
	cat ./project/safety.sbt | grep `cat ./VERSION`

test_unit_test:
	sbt test

# https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html
test_plugin:
	sbt 'set isSnapshot := true' scripted

test_coverage_run:
	sbt clean coverage test coverageReport
	open ./target/scala-2.12/sbt-1.0/scoverage-report/index.html

test_coverage:
	sbt clean coverage test

test_coverage_report:
	sbt coverageReport && sbt coverageAggregate

check_style:
	sbt safetyCheckScalaFmt || echo "Need to add the plugin to itself"

test: deep_clean publishLocal check_style test_coverage test_plugin test_coverage_report
