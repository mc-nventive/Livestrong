ant full-release
cp -f bin/Livestrong-release-unsigned.apk bin/Livestrong-release-signed.apk
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore signing/my-release-key.keystore bin/Livestrong-release-signed.apk nventive
jarsigner -verify bin/Livestrong-release-signed.apk
if [ -f bin/Livestrong.apk ]; then rm bin/Livestrong.apk; fi
zipalign -v 4 bin/Livestrong-release-signed.apk bin/Livestrong.apk
echo "See bin/Livestrong.apk for the resulting APK."
