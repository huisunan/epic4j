#!/bin/bash
function start() {
  cd /opt/epic4j || exit
  if [ -e "epic4j.jar.update" ];then
    rm -rf epic4j.jar
    mv epic4j.jar.update epic4j.jar
    echo "update success"
  fi
  java -jar epic4j.jar
  res=$?
  return $res
}

start
status=$?
while [ $status -eq 66 ]
do
  start
  status=$?
  echo "update event"
done