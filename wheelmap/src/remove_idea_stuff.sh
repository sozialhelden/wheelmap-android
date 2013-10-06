TARGETDIR=../..

checkIfAvailableAndRemove() {

  if [ -d $1 ]; then
    echo "Deleting $1"
    rm -rfv $1;
  fi
}

checkIfAvailableAndRemove $TARGETDIR/out
checkIfAvailableAndRemove TARGETDIR/.idea
find $TARGETDIR -name \*.iml|xargs rm -rfv
find $TARGETDIR -name gen-\*|xargs rm -rfv
