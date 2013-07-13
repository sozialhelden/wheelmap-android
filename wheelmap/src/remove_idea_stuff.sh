TARGETDIR=../..

rm -rf $TARGETDIR/out
rm -rf $TARGETDIR/.idea
find $TARGETDIR -name \*.iml|xargs rm -rf
find $TARGETDIR -name gen-\*|xargs rm -rf
