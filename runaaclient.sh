#!/bin/bash

AA_HOME=/home/james

source ${AA_HOME}/setenv.sh

cd ${AA_HOME}/client
java uk.ac.ed.epcc.simpleaa.SimpleAAClient query $1
