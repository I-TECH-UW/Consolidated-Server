#!/bin/bash

tar -xvf dockerImages.tar.gz

docker load < dockerImages/openhim-core.tar.gz
docker load < dockerImages/nginx.tar.gz
docker load < dockerImages/openhim-console.tar.gz
docker load < dockerImages/hapi.tar.gz
docker load < dockerImages/mongo.tar.gz
docker load < dockerImages/postgres.tar.gz
