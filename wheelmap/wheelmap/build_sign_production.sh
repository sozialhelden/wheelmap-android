export ANDROID_HOME=/Users/torstenlemm/Documents/android-sdk-macosx
mvn clean package  -Pproduction -DskipTests=true -Palternative-signature -Dsign.keystore=/Users/torstenlemm/Downloads/Wheelmap_2_Android/sozialhelden.keystore -Dsign.alias=sozialhelden -Dsign.storepass='IrkZun0L$pL0M$zX' -Dsign.keypass='TgM9Lfr*m79uBs.e'
