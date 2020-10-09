#!/bin/bash

psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -a -f /database/platform/ddl/sequence/sequence.sql

for file in /database/platform/ddl/*.sql
do
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -a -f ${file}
done

for file in /database/platform/dml/*.sql
do
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -a -f ${file}
done

for file in /database/platform/index/*.sql
do
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -a -f ${file}
done
