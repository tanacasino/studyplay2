#!/bin/bash

SCRIPTS_DIR=$(cd $(dirname "$0") && pwd)

mysql -u root < "$SCRIPTS_DIR/create-schema.sql"

