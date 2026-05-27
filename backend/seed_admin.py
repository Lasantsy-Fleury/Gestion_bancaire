# -*- coding: utf-8 -*-
from werkzeug.security import generate_password_hash
from app.database import get_db_connection


def seed_admin(username, password):
    conn = get_db_connection()
    if not conn:
        print("Connexion a la base impossible.")
        return
    try:
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM Users WHERE username = %s", (username,))
        if cursor.fetchone():
            print("Utilisateur deja existant.")
            return
        password_hash = generate_password_hash(password)
        cursor.execute(
            "INSERT INTO Users (username, password_hash) VALUES (%s, %s)",
            (username, password_hash),
        )
        conn.commit()
        print("Utilisateur admin cree.")
    except Exception as exc:
        print(f"Erreur seed admin: {exc}")
    finally:
        cursor.close()
        conn.close()


if __name__ == "__main__":
    seed_admin("LASANTSY", "root1234")
