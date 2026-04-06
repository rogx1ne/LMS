#!/bin/bash
# LMS Launcher
cd "$(dirname "$0")"
export LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"
java -cp "bin:lib/*" -Doracle.jdbc.timezoneAsRegion=false Main
