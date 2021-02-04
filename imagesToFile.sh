#!/bin/bash
mkdir dockerImages

docker save jembi/openhim-core | gzip > dockerImages/openhim-core.tar.gz
docker save nginx:latest | gzip > dockerImages/nginx.tar.gz
docker save jembi/openhim-console | gzip > dockerImages/openhim-console.tar.gz
docker save hapiproject/hapi:latest | gzip > dockerImages/hapi.tar.gz
docker save mongo:3.4 | gzip > dockerImages/mongo.tar.gz
docker save postgres:12-alpine | gzip > dockerImages/postgres.tar.gz

tar -czvf dockerImages.tar.gz dockerImages