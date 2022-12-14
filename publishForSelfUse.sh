#!/usr/bin/env bash

V=`echo $1 | cut -c2-`
echo "$V"
sbt -Dwithout_self_use ";set version := \"${V}\" ;publishLocal"
