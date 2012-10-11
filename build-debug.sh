ant full-debug-prod
cp -f bin/Livestrong-debug-unsigned.apk bin/Livestrong-debug-signed.apk
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore signing/my-debug-key.keystore bin/Livestrong-debug-signed.apk 2xm
jarsigner -verify bin/Livestrong-debug-signed.apk
if [ -f bin/Livestrong.apk ]; then rm bin/Livestrong.apk; fi
zipalign -v 4 bin/Livestrong-debug-signed.apk bin/Livestrong.apk
echo "See bin/Livestrong.apk for the resulting APK."

if [ -d $HOME/Downloads/Livestrong-android-source/ ]; then
    rm -rf $HOME/Downloads/Livestrong-android-source/
fi

mkdir -p $HOME/Downloads/Livestrong-android-source/
rsync -a --exclude '*.launch' --exclude 'bin/' --exclude 'local.properties' * $HOME/Downloads/Livestrong-android-source/
(cd $HOME/Downloads/; zip -r9 Livestrong-android-source.zip Livestrong-android-source/; rm -rf Livestrong-android-source/)
