#!/usr/bin/env bash

V=`echo $1 | cut -c2-`
echo "$V"
sbt -Dwithout_self_use ";clean ;set version := \"${V}\" ;publishLocal"
