ant full-debug-prod
cp -f bin/Livestrong-debug.apk bin/Livestrong-debug-signed.apk
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore signing/my-release-key.keystore bin/Livestrong-debug-signed.apk nventive
jarsigner -verbose -certs -verify bin/Livestrong-debug-signed.apk
if [ -f bin/Livestrong.apk ]; then rm bin/Livestrong.apk; fi
zipalign -v 4 bin/Livestrong-debug-signed.apk bin/Livestrong.apk
echo "See bin/Livestrong.apk for the resulting APK."
