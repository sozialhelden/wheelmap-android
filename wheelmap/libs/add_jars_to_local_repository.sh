mvn install:install-file -Dfile=libGoogleAnalytics.jar -DgroupId=org.wheelmap.android \
    -DartifactId=googleanalytics -Dversion=1.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=mapsforge-map-0.2.4.jar -DgroupId=org.wheelmap.android \
    -DartifactId=mapsforge-map -Dversion=0.2.4 -Dpackaging=jar