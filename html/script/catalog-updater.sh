#!/bin/bash
# You need sshpass installed for this script to work

FOLDER=/home/tsagrista/Gaia/data/HYG/hyg-days
DATE=`date +%Y%m%d -d "today"`
FILE=$FOLDER/hygxyz-$DATE.bin

echo "Copying file $FILE to hercules host"


# Copy file to server via SCP
sshpass -f ./passfile scp $FILE "tsagrista@hercules:/work/www-staff/html/gaiasandbox/fov/"

# Grant read permissions
sshpass -f ./passfile ssh "tsagrista@hercules" 'cd /work/www-staff/html/gaiasandbox/fov/ && chmod ugo+r *.bin && exit'