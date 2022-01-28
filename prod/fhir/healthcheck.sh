#!/bin/bash

#get location of this script
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  scriptDir="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
scriptDir="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

if [ $(curl -k --fail --silent --cert-type P12 --cert /usr/local/tomcat/certs/keystore:passwordForKeystore https://host.openelis.org:8444/fhir/Task?status=requested -o /dev/null -w '%{http_code}' -s) == "200" ]; then 
	exit 0;
else
	exit 1;
fi
