#!/bin/bash

export CLASSPATH=bcprov-jdk16-145.jar

CERTSTORE=../res/raw/livestrongstore.bks
if [ -a $CERTSTORE ]; then
    rm $CERTSTORE || exit 1
fi

CERTPASS="changeit"

keytool -import -v -trustcacerts -alias 0 -file <(openssl x509 -in 'COMODO Certification Authority.crt') -keystore $CERTSTORE -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath /usr/share/java/bcprov.jar -storepass $CERTPASS
keytool -import -v -trustcacerts -alias 1 -file <(openssl x509 -in 'EssentialSSL CA.crt') -keystore $CERTSTORE -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath /usr/share/java/bcprov.jar -storepass $CERTPASS
keytool -import -v -trustcacerts -alias 2 -file <(openssl x509 -in 'service.livestrong.com.crt') -keystore $CERTSTORE -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath /usr/share/java/bcprov.jar -storepass $CERTPASS
