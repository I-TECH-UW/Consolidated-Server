#!/bin/bash

# Author : Caleb Steele-Lane

read -p "server address: " SERVER
echo

read -sp "database admin password: " DB_ADMIN_PASSWORD
echo

read -sp "database password: " DB_PASSWORD
echo

read -sp "keystore password: " KEY_PASS
echo

read -sp "truststore password: " TRUST_PASS
echo


find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/host\.openelis\.org/$SERVER/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/databaseAdminPassword/$DB_ADMIN_PASSWORD/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/databasePassword/$DB_PASSWORD/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/passwordForKeystore/$KEY_PASS/g"
find ./ \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i "s/passwordForTruststore/$TRUST_PASS/g"