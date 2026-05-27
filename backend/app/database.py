# -*- coding: utf-8 -*-
import os

if os.name == "nt":
    os.environ.setdefault("PGCLIENTENCODING", "LATIN1")
    os.environ.setdefault("PGOPTIONS", "-c lc_messages=C")
    os.environ.setdefault("LC_ALL", "C")
    os.environ.setdefault("LANG", "C")

import psycopg2
from psycopg2 import Error

def get_db_connection():
    client_encoding = os.environ.get("PGCLIENTENCODING", "LATIN1")
    db_host = os.environ.get("DB_HOST", "localhost")
    db_name = os.environ.get("DB_NAME", "gestion_bancaire")
    db_user = os.environ.get("DB_USER", "postgres")
    db_password = os.environ.get("DB_PASSWORD", "root1234")
    db_port = os.environ.get("DB_PORT", "5432")
    try:
        connection = psycopg2.connect(
            host=db_host,
            database=db_name,
            user=db_user,
            password=db_password,
            port=db_port,
            options=f"-c client_encoding={client_encoding} -c lc_messages=C"
        )
        connection.set_client_encoding(client_encoding)
        return connection
    except UnicodeDecodeError:
        print("Erreur de connexion a PostgreSQL: message d'erreur non decodable. Verifiez le mot de passe, la base et le service.")
        return None
    except Exception as e:
        print(f"Erreur de connexion à PostgreSQL: {e}")
        return None
