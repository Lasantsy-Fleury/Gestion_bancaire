# Application de Gestion Bancaire (Full-Stack)

Ce projet est une application bancaire complète composée d'un backend REST en Python (Flask), d'une base de données PostgreSQL, et d'un client lourd en Java Swing.

## Architecture

* **`database/`** : Contient le script `schema.sql` pour initialiser la base de données PostgreSQL (tables `Client`, `Versement`, `Retrait`).
* **`backend/`** : Serveur REST Flask exposant les API de gestion des clients et des transactions bancaires.
* **`frontend/`** : Application bureau Java 11+ utilisant Swing pour l'interface utilisateur et `java.net.http.HttpClient` pour communiquer avec l'API.

## Prérequis

- **PostgreSQL** installé et en cours d'exécution (Port 5432).
- **Python 3.8+** avec `pip`.
- **JDK 11+** et **Maven** (pour le frontend).

## Installation et Lancement

### 1. Base de données
1. Créez une base de données nommée `gestion_bancaire` dans PostgreSQL.
2. Exécutez le script `database/schema.sql` dans cette base.

### 2. Backend (Python Flask)
1. Ouvrez un terminal dans le dossier du projet.
2. (Optionnel) Créez un environnement virtuel : `python -m venv .venv` puis activez-le: `.venv\Scripts\activate`.
3. Installez les dépendances :
   ```bash
   pip install -r backend/requirements.txt
   ```
4. Modifiez le mot de passe PostgreSQL dans le fichier `backend/app/database.py` (ligne 10) si nécessaire (actuellement configuré sur '123').
5. Lancez le serveur :
   ```bash
   python backend/run.py
   ```
   Le serveur sera accessible sur `http://localhost:5000`.

### 3. Frontend (Java Swing)
L'application client possède 6 onglets asynchrones (via `SwingWorker`) couvrant toutes les fonctionnalités (CRUD, Mouvements, État absolu).
Pour la lancer :
- Ouvrez le dossier `frontend/` dans votre IDE (IntelliJ, Eclipse, VS Code).
- Ou lancez-la via Maven :
   ```bash
   cd frontend
   mvn clean compile exec:java "-Dexec.mainClass=com.banque.ClientBancaireSwing"
   ```

## Fonctionnalités Métier

- **Recalcul absolu** : Lors d'une transaction, le solde n'est pas simplement incrémenté. Il est recalculé formellement : `Solde = Σ(Versements) - Σ(Retraits)`.
- **Transfert** : Opération atomique créant un Retrait chez la source et un Versement chez la destination.
- **Asynchronisme** : L'interface graphique Java ne gèle jamais pendant les appels réseau grâce à l'implémentation stricte des `SwingWorker`.
"# Gestion_bancaire" 
