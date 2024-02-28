#!/usr/bin/env bash

./gradlew clean publish
./gradlew natives:ctpapi:clean natives:ctpapi:publish -PV=v6.6.1_P1_20210406
./gradlew natives:ctpapi:clean natives:ctpapi:publish -PV=6.3.13_20181119
./gradlew natives:ctpapi:clean natives:ctpapi:publish -PV=6.3.15_20190220
