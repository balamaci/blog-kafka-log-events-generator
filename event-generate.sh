#!/usr/bin/env bash
if [ $# -eq 0 ]
  then
    echo "Usage: event-generate.sh kafka_container_name"
    exit 1
fi

docker run -it --link $1:kafka --rm --name event_generator -v ~/.m2/:/root/.m2 -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven maven:latest mvn clean compile exec:java