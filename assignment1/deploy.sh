#!/bin/bash

cd src
phonegap build android
cd ..
echo 'Uninstalling app'
adb uninstall uk.ac.aber.dcs.slj11.conference
echo 'Reinstalling...'
adb -s f2963c9 install src/platforms/android/build/outputs/apk/android-debug.apk
