
# Finansaurus API
This repository represents the server part of the Finansaurus project. It allows to store the transactions, accounts, etc... in a database and verifies the various inputs.

# Database
A MySQL database is required to store the transactions and other data.

MariaDB is a good option as it's lightweight and fast (https://mariadb.com/).

# Installing with Docker
The following configuration can be used with docker compose:
```
version: '3.9'
services:
  api:
    image: wouternivelle/finansaurus-api:1.2.1
    restart: unless-stopped
    environment:
      - MARIADB_HOST=mariadb
      - MARIADB_PASSWORD=password_of_database
      - MARIADB_USER=user_of_database
    ports:
      - 8181:8080
    depends_on:
      - db
  db:
    image: mariadb    
    hostname: mariadb
    restart: unless-stopped
    container_name: finansaurus-db
    environment:
      - MYSQL_RANDOM_ROOT_PASSWORD=yes
      - MYSQL_USER=user_of_database
      - MYSQL_PASSWORD=password_of_database
      - MYSQL_DATABASE=user_of_database
    volumes:
      - /path/to/local/db/config:/var/lib/mysql
      - /path/to/local/db/config/my.cnf:/etc/mysql/conf.d/my.cnf
```
