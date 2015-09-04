#!/bin/bash
# You need sshpass installed for this script to work

FOLDER=/home/tsagrista/Gaia/data/HYG/hyg-days
DATE=`date +%Y%m%d -d "today"`
FILE=$FOLDER/hygxyz-$DATE.bin

USER=tsagrista
SERVER=hercules

# Copy catalog file to server via SCP
echo "Copying $FILE to $SERVER host"
CMD2=`sshpass -f ./passfile scp $FILE "$USER@$SERVER:/work/www-staff/html/gaiasandbox/focalplane/assets/data/hygxyz.bin"`
