# -*- coding: utf-8 -*-
from werkzeug.security import generate_password_hash, check_password_hash
from .database import get_db_connection


class UserModel:
    @staticmethod
    def create_user(username, password):
        conn = get_db_connection()
        if not conn:
            return None
        try:
            cursor = conn.cursor()
            password_hash = generate_password_hash(password)
            cursor.execute(
                "INSERT INTO Users (username, password_hash) VALUES (%s, %s) RETURNING id",
                (username, password_hash),
            )
            user_id = cursor.fetchone()[0]
            conn.commit()
            return user_id
        except Exception as e:
            print(f"Error create user: {e}")
            return None
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def find_by_username(username):
        conn = get_db_connection()
        if not conn:
            return None
        try:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT id, username, password_hash FROM Users WHERE username = %s",
                (username,),
            )
            row = cursor.fetchone()
            if not row:
                return None
            return {"id": row[0], "username": row[1], "password_hash": row[2]}
        except Exception as e:
            print(f"Error find user: {e}")
            return None
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def verify_password(password, password_hash):
        return check_password_hash(password_hash, password)

class ClientModel:
    @staticmethod
    def create(numeroCompte, nom, adresse, solde=0):
        conn = get_db_connection()
        if not conn: return False
        try:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO Client (numeroCompte, Nom, Adresse, Solde) VALUES (%s, %s, %s, %s)", 
                           (numeroCompte, nom, adresse, solde))
            conn.commit()
            return True
        except Exception as e:
            print(f"Error create client: {e}")
            return False
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def delete(numeroCompte):
        conn = get_db_connection()
        if not conn: return False
        try:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM Client WHERE numeroCompte = %s", (numeroCompte,))
            deleted = cursor.rowcount > 0
            conn.commit()
            return deleted
        except Exception as e:
            print(f"Error delete client: {e}")
            return False
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def update(numeroCompte, nom, adresse, solde):
        conn = get_db_connection()
        if not conn: return False
        try:
            cursor = conn.cursor()
            # COALESCE permet de garder l'ancienne valeur si la nouvelle est null
            cursor.execute("""
                UPDATE Client 
                SET Nom = COALESCE(%s, Nom), 
                    Adresse = COALESCE(%s, Adresse),
                    Solde = COALESCE(%s, Solde)
                WHERE numeroCompte = %s
            """, (nom, adresse, solde, numeroCompte))
            updated = cursor.rowcount > 0
            conn.commit()
            return updated
        except Exception as e:
            print(f"Error update client: {e}")
            return False
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def search(query):
        conn = get_db_connection()
        if not conn: return []
        try:
            cursor = conn.cursor()
            search_pattern = f"%{query}%"
            # ILIKE permet une recherche insensible à la casse sous PostgreSQL
            cursor.execute("""
                SELECT numeroCompte, Nom, Adresse, Solde 
                FROM Client 
                WHERE numeroCompte ILIKE %s OR Nom ILIKE %s OR Adresse ILIKE %s
            """, (search_pattern, search_pattern, search_pattern))
            rows = cursor.fetchall()
            clients = [{"numeroCompte": r[0], "nom": r[1], "adresse": r[2], "solde": r[3]} for r in rows]
            return clients
        except Exception as e:
            print(f"Error search client: {e}")
            return []
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def get_etat_clients():
        conn = get_db_connection()
        if not conn: return []
        try:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT 
                    c.numeroCompte, 
                    c.Nom, 
                    (SELECT COALESCE(SUM(Montant_Versement), 0) FROM Versement WHERE numeroCompte = c.numeroCompte) 
                    - 
                    (SELECT COALESCE(SUM(Montant_Retrait), 0) FROM Retrait WHERE numeroCompte = c.numeroCompte) 
                    AS soldeCalcule
                FROM 
                    Client c
            """)
            rows = cursor.fetchall()
            etats = []
            for r in rows:
                etats.append({
                    "numCompte": r[0],
                    "nom": r[1],
                    "soldeCalcule": int(r[2]) if r[2] is not None else 0
                })
            return etats
        except Exception as e:
            print(f"Error get_etat_clients: {e}")
            return []
        finally:
            cursor.close()
            conn.close()

class TransactionModel:
    @staticmethod
    def _recalculate_solde(cursor, numeroCompte):
        # Cette requête va calculer le vrai solde en faisant la somme des versements et en soustrayant la somme des retraits
        cursor.execute("""
            UPDATE Client 
            SET Solde = (
                SELECT COALESCE(SUM(Montant_Versement), 0) FROM Versement WHERE numeroCompte = %s
            ) - (
                SELECT COALESCE(SUM(Montant_Retrait), 0) FROM Retrait WHERE numeroCompte = %s
            )
            WHERE numeroCompte = %s
        """, (numeroCompte, numeroCompte, numeroCompte))

    @staticmethod
    def versement(numeroCompte, montant, date_tx):
        conn = get_db_connection()
        if not conn: return False
        try:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO Versement (numeroCompte, Montant_Versement, dateVersement) VALUES (%s, %s, %s)", (numeroCompte, montant, date_tx))
            
            # Recalcul strict et complet du solde après l'ajout
            TransactionModel._recalculate_solde(cursor, numeroCompte)
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            print(f"Error versement: {e}")
            return False
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def retrait(numeroCompte, numeroCheque, montant, date_tx):
        conn = get_db_connection()
        if not conn: return False
        try:
            cursor = conn.cursor()
            # Contrôle préalable : le solde est-il suffisant ?
            cursor.execute("SELECT Solde FROM Client WHERE numeroCompte = %s", (numeroCompte,))
            row = cursor.fetchone()
            if not row or row[0] < montant:
                return False
                
            cursor.execute("INSERT INTO Retrait (numeroCompte, numeroCheque, Montant_Retrait, DateRetrait) VALUES (%s, %s, %s, %s)", (numeroCompte, numeroCheque, montant, date_tx))
            
            # Recalcul strict et complet du solde après l'ajout
            TransactionModel._recalculate_solde(cursor, numeroCompte)
            conn.commit()
            return True
        except Exception as e:
            conn.rollback()
            print(f"Error retrait: {e}")
            return False
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def transfert(compteSource, compteDestination, montant, date_tx):
        conn = get_db_connection()
        if not conn: return False, "Erreur de connexion à la base de données"
        try:
            cursor = conn.cursor()
            
            # Vérifier si les comptes existent et si le solde source est suffisant
            cursor.execute("SELECT Solde FROM Client WHERE numeroCompte = %s", (compteSource,))
            row_source = cursor.fetchone()
            if not row_source:
                return False, "Le compte source n'existe pas"
            if row_source[0] < montant:
                return False, "Solde insuffisant sur le compte source"
                
            cursor.execute("SELECT numeroCompte FROM Client WHERE numeroCompte = %s", (compteDestination,))
            if not cursor.fetchone():
                return False, "Le compte destination n'existe pas"
                
            # Débiter compteSource (ajout d'un retrait)
            cursor.execute("INSERT INTO Retrait (numeroCompte, numeroCheque, Montant_Retrait, DateRetrait) VALUES (%s, %s, %s, %s)", 
                           (compteSource, 'TRANSFERT', montant, date_tx))
                           
            # Créditer compteDestination (ajout d'un versement)
            cursor.execute("INSERT INTO Versement (numeroCompte, Montant_Versement, dateVersement) VALUES (%s, %s, %s)", 
                           (compteDestination, montant, date_tx))
                           
            # Recalculer les soldes pour les deux comptes
            TransactionModel._recalculate_solde(cursor, compteSource)
            TransactionModel._recalculate_solde(cursor, compteDestination)
            
            conn.commit()
            return True, "Transfert effectué avec succès"
        except Exception as e:
            conn.rollback()
            print(f"Error transfert: {e}")
            return False, "Erreur interne lors du transfert"
        finally:
            cursor.close()
            conn.close()

    @staticmethod
    def get_mouvements(numeroCompte):
        conn = get_db_connection()
        if not conn: return []
        try:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT 'VERSEMENT' as type, 
                       NULL as numCheque, 
                       Montant_Versement as montant, 
                       dateVersement as date
                FROM Versement
                WHERE numeroCompte = %s
                
                UNION ALL
                
                SELECT 'RETRAIT' as type, 
                       numeroCheque as numCheque, 
                       Montant_Retrait as montant, 
                       DateRetrait as date
                FROM Retrait
                WHERE numeroCompte = %s
                
                ORDER BY date ASC
            """, (numeroCompte, numeroCompte))
            
            rows = cursor.fetchall()
            mouvements = []
            for r in rows:
                mouvements.append({
                    "type": r[0],
                    "numCheque": r[1],
                    "montant": r[2],
                    "date": r[3]
                })
            return mouvements
        except Exception as e:
            print(f"Error get_mouvements: {e}")
            return []
        finally:
            cursor.close()
            conn.close()
