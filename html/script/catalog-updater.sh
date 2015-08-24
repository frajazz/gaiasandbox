#!/bin/bash
# You need sshpass installed for this script to work

FOLDER=/home/tsagrista/Gaia/data/HYG/hyg-days
DATE=`date +%Y%m%d -d "today"`
FILE=$FOLDER/hygxyz-$DATE.bin

USER=tsagrista
SERVER=hercules

# Copy catalog file to server via SCP
echo "Copying $FILE to $SERVER host"
CMD2=`sshpass -f ./passfile scp $FILE "$USER@$SERVER:/work/www-staff/html/gaiasandbox/fov/android/assets/data/hygxyz.bin"`

# Grant read permissions
echo "Granting read permissions to $USER@$SERVER:$SERV_FOLDER/hygxyz.bin"
CMD3=`sshpass -f ./passfile ssh "$USER@$SERVER" 'chmod ugo+r /work/www-staff/html/gaiasandbox/fov/android/assets/data/hygxyz.bin && exit'`