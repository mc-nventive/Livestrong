ant full-release
cp -f bin/Livestrong-release-unsigned.apk bin/Livestrong-release-signed.apk
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore signing/demandmedia-old-app.keystore bin/Livestrong-release-signed.apk livestrong
jarsigner -verify bin/Livestrong-release-signed.apk
if [ -f bin/Livestrong.apk ]; then rm bin/Livestrong.apk; fi
zipalign -v 4 bin/Livestrong-release-signed.apk bin/Livestrong.apk
echo "See bin/Livestrong.apk for the resulting APK."

if [ -d $HOME/Downloads/Livestrong-android-source/ ]; then
    rm -rf $HOME/Downloads/Livestrong-android-source/
fi

mkdir -p $HOME/Downloads/Livestrong-android-source/
rsync -a --exclude '*.launch' --exclude 'bin/' --exclude 'local.properties' * $HOME/Downloads/Livestrong-android-source/
(cd $HOME/Downloads/; zip -r9 Livestrong-android-source.zip Livestrong-android-source/; rm -rf Livestrong-android-source/)
