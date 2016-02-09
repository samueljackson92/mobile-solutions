#!/bin/bash

cd ./src
phonegap build android
echo 'Uninstalling app'
adb uninstall uk.ac.aber.dcs.slj11.conference
echo 'Reinstalling...'
adb -s f2963c9 install platforms/android/build/outputs/apk/android-debug.apk
cd ..
