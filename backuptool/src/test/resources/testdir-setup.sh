#!/bin/bash -xv

BASEDIR=/tmp/backupTest

ECHO="echo -e"
FIND=find

# Sanity check to not delete anything important
if [ "${BASEDIR%/*}" = "/tmp" ];
then
   rm -rf "$BASEDIR"/*
fi

mkdir -p "$BASEDIR"/{a,b,c}/{a,b,c}/logs
mkdir -p "$BASEDIR"/{a,b,c}/logs/{a,b,c}/logs/{a,b,c}
mkdir -p "$BASEDIR"/{a,b,c}/testlogs/{a,b,c}/logstest/{a,b,c}
mkdir -p "$BASEDIR"/{a,b,c}/logstest/{a,b,c}/logs/{a,b,c}
mkdir -p "$BASEDIR/d"
mkdir -p "$BASEDIR/*"/{a,b,c}
mkdir -p "$BASEDIR/\*"/{a,b,c}
mkdir -p "$BASEDIR/\\\*"/{a,b,c}
mkdir -p "$BASEDIR/a b"/{a,b,c}
mkdir -p "$BASEDIR/holdingDirectory/"
mkdir -p "$BASEDIR/backups/"
mkdir -p "$BASEDIR/archivetest01"/{a,b,c}/{a,b,c}/{a,b,c}
mkdir -p "$BASEDIR/tmp"

head -c 510 /dev/urandom > "$BASEDIR/archivetest01/b/testfile00001.bin"
tar cvf "$BASEDIR/archivetest01/b/testArch00001.btaf" "$BASEDIR/archivetest01/b/testfile00001.bin"

head -c 610 /dev/urandom > "$BASEDIR/archivetest01/c/testfile00002.bin"
tar cvf "$BASEDIR/archivetest01/c/testArch00001.btaf" "$BASEDIR/archivetest01/c/testfile00002.bin"

head -c 710 /dev/urandom > "$BASEDIR/archivetest01/c/testfile00003.bin"
tar cvf "$BASEDIR/archivetest01/c/testArch00002.btaf" "$BASEDIR/archivetest01/c/testfile00003.bin"

head -c 1G /dev/urandom > "$BASEDIR/archivetest01/c/a/testfile00004.bin"
head -c 1M /dev/urandom > "$BASEDIR/archivetest01/c/a/testfile00005.bin"
head -c 2M /dev/urandom > "$BASEDIR/archivetest01/c/a/testfile00006.bin"
head -c 10M /dev/urandom > "$BASEDIR/archivetest01/c/a/testfile00007.bin"

for I in {1..5000}
do
    SIZE=$(($I % 100))
    SIZE_PADDED=$(printf "%0.4d" $I)
    FLDNUM=$(($I % 4))

    case $FLDNUM in
      1)
         FLD="a"
         ;;
      2)
         FLD="b"
         ;;
      3)
         FLD="c"
         ;;
      *)
         FLD=""
         ;;
    esac

    head -c ${SIZE}K /dev/urandom > "$BASEDIR/archivetest01/c/b/${FLD}/testfile${SIZE_PADDED}.bin"
done

$ECHO "
test file 1\n
" >> "$BASEDIR/test-01.txt"

$ECHO "
test file 2\n
" >> "$BASEDIR/c/test.txt"

$ECHO "
log file\n
" >> "$BASEDIR/c/logFile.log"

$ECHO "
log file 2\n
" >> "$BASEDIR/c/logFile2.log"

$ECHO "
data file 01\n
" >> "$BASEDIR/c/a/file01.dat"

$ECHO "
file 02\n
" >> "$BASEDIR/c/a/file02.dat"

$ECHO "
file 03\n
" >> "$BASEDIR/c/file03.dat"

$ECHO "
test file 03\n
" >> "$BASEDIR/b/a/test-03.txt"

$ECHO "
test file 04\n
" >> "$BASEDIR/b/b/test-04.txt"

$ECHO "
test file 05\n
" >> "$BASEDIR/b/c/test-05.txt"

$ECHO "
test file 06\n
" >> "$BASEDIR/b/a/test-06.txt"

$ECHO "
test file 07\n
" >> "$BASEDIR/b/b/test-07.txt"

$ECHO "
test file 08\n
" >> "$BASEDIR/b/c/test-08.txt"

$ECHO "
test log 09\n
" >> "$BASEDIR/b/test-09.log"

$ECHO "
test log 10\n
" >> "$BASEDIR/b/test-10.log"

$ECHO "
test log 11\n
" >> "$BASEDIR/b/test-11.log"

$ECHO "
test log 12\n
" >> "$BASEDIR/b/logs/test-12.log"

$ECHO "
test data 13\n
" >> "$BASEDIR/b/logs/test-13.dat"

$ECHO "
test data 14\n
" >> "$BASEDIR/b/test-14.dat"

$ECHO "
test log 15\n
" >> "$BASEDIR/c/a/logs/test-15.log"

$ECHO "
test dat 16\n
" >> "$BASEDIR/c/a/logs/test-16.dat"

if [ ! -h "$BASEDIR/c/hlink-17.dat" ];
then
    ln "$BASEDIR/b/logs/test-13.dat" "$BASEDIR/c/hlink-17.dat"
fi

if [ ! -h "$BASEDIR/c/c/slink-18.log" ];
then
    ln -s "$BASEDIR/c/a/logs/test-15.log" "$BASEDIR/c/c/slink-18.dat"
fi

if [ ! -h "$BASEDIR/c/c/slink-20.dat" ];
then
    ln -s "$BASEDIR/c/c/slink-18.dat" "$BASEDIR/c/c/slink-20.dat"
fi

if [ ! -h "$BASEDIR/c/c-link-21" ];
then
    ln -s "$BASEDIR/c/c" "$BASEDIR/c/c-link-21"
fi

if [ ! -h "$BASEDIR/e/c-link-loop-22" ];
then
    cp -a "$BASEDIR/c" "$BASEDIR/e" 

    ln -s "$BASEDIR/e/c" "$BASEDIR/e/c-link-loop-22"
    ln -s "$BASEDIR/e/c-link-loop-22" "$BASEDIR/e/c/logs/c-link-loop-dest"
fi

mkdir -p "$BASEDIR/e/c/logs/"{1,a,b,c,z}

if [ ! -h "$BASEDIR/e/non-existent-link-19.log" ];
then
    touch "$BASEDIR/file-does-not-exist"
    ln -s "$BASEDIR/file-does-not-exist" "$BASEDIR/e/non-existent-link-19.log" 
    rm "$BASEDIR/file-does-not-exist"
fi

if [ ! -h "$BASEDIR/e/non-existant-link-23" ];
then
    mkdir "$BASEDIR/folder-does-not-exist"
    ln -s "$BASEDIR/folder-does-not-exist" "$BASEDIR/e/non-existant-link-23" 
    rmdir "$BASEDIR/folder-does-not-exist"
fi

# Sentinel time value by default
$FIND "$BASEDIR" -type f -exec touch -t '199001010000' {} \;
$FIND "$BASEDIR" -type d -exec touch -t '198501010000' {} \;

# Build the listing files
$FIND "$BASEDIR" > "$BASEDIR/find01.txt"
$FIND "$BASEDIR" -type f > "$BASEDIR/find01-files.txt"
$FIND "$BASEDIR" -type l > "$BASEDIR/find01-links.txt"
$FIND "$BASEDIR" -type d > "$BASEDIR/find01-dirs.txt"

true > "$BASEDIR/find02.txt"
while read currFile; do
echo "expectedSet.add(Paths.get(\"$currFile\"));" >> "$BASEDIR/find02.txt"
done < "$BASEDIR/find01.txt"

true > "$BASEDIR/find02-files.txt"
while read currFile; do
echo "expectedSet.add(Paths.get(\"$currFile\"));" >> "$BASEDIR/find02-files.txt"
done < "$BASEDIR/find01-files.txt"

true > "$BASEDIR/find02-links.txt"
while read currFile; do
echo "expectedSet.add(Paths.get(\"$currFile\"));" >> "$BASEDIR/find02-links.txt"
done < "$BASEDIR/find01-links.txt"

true > "$BASEDIR/find02-dirs.txt"
while read currFile; do
echo "expectedSet.add(Paths.get(\"$currFile\"));" >> "$BASEDIR/find02-dirs.txt"
done < "$BASEDIR/find01-dirs.txt"

