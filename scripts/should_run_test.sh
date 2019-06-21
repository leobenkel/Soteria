#!/usr/bin/env bash

if [[ -z ${TRAVIS_COMMIT} ]]; then
   echo "Could not find 'TRAVIS_COMMIT'. Run test to be sure"
   exit 0
fi

FILES=$(git diff --name-only master...${TRAVIS_COMMIT})

if [[ -z ${FILES} ]]; then
    echo "Did not find any file changed for commit hash: '${TRAVIS_COMMIT}'. No need to run test."
    exit 1
fi

for FILE in ${FILES}; do
    if [[ ${FILE} =~ ^src/ || ${FILE} =~ .sbt$ || ${FILE} =~ .json$ || ${FILE} =~ .conf$ || ${FILE} =~ .xml$ || ${FILE} =~ .yml$ ]]; then
        echo "Need to run test! Because of '${FILE}'"
        exit 0
    fi
done

echo "Did not find critical files. No test run. Among:"
echo "${FILES}"
exit 1
