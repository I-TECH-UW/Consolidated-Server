#!/bin/sh

# Author : Caleb Steele-Lane

echo -n server address: 
read SERVER
echo

echo -n database admin password: 
read -s DB_ADMIN_PASSWORD
echo

echo -n database password: 
read -s DB_PASSWORD
echo

echo -n keystore password: 
read -s KEY_PASS
echo

echo -n truststore password: 
read -s TRUST_PASS
echo


find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/host\.openelis\.org/$SERVER/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/databaseAdminPassword/$DB_ADMIN_PASSWORD/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/databasePassword/$DB_PASSWORD/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/passwordForKeystore/$KEY_PASS/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/passwordForTruststore/$TRUST_PASS/g"