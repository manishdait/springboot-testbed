#!/bin/bash
echo -e "POSTGRES_USERNAME=postgres\n"\
"POSTGRES_PASSWORD=password\n"\
"POSTGRES_DB=todo-db\n"\
"POSTGRES_PORT=5432\n" >> ../.env

docker compose -f ../docker-compose.yml up -d
