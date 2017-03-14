#!/usr/bin/env bash

cd ..

if [ -e persistent/application-mail.extension ]
then
    cat persistent/application-mail.extension >> webifier-mail/src/main/resources/application.properties
fi

cd webifier-mail
./gradlew :build
cd ..

cd run

rm -f webifier-mail-*.jar
rm -f start-mail.sh

cp ../webifier-mail/build/libs/webifier-mail-*.jar .

JAR=$(ls| grep 'webifier\-mail\-.*\.jar')

cat > start-mail.sh << EOF
killall webifier-mail

LD_PRELOAD=../persistent/libprocname.so PROCNAME=webifier-mail java -jar ${JAR} > output-mail.log 2>&1 &
EOF

chmod +x start-mail.sh