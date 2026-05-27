-- Nettoyage des anciennes tables si elles existent
DROP TABLE IF EXISTS Retrait CASCADE;
DROP TABLE IF EXISTS Versement CASCADE;
DROP TABLE IF EXISTS Client CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- 1. Création des tables
CREATE TABLE Client (
    numeroCompte VARCHAR(20) PRIMARY KEY,
    Nom VARCHAR(100) NOT NULL,
    Adresse VARCHAR(255) DEFAULT 'Non renseigné',
    Solde INT NOT NULL DEFAULT 0
);

CREATE TABLE Users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(80) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Versement (
    idVersement SERIAL PRIMARY KEY,
    numeroCompte VARCHAR(20) NOT NULL,
    Montant_Versement INT NOT NULL,
    dateVersement VARCHAR(30) NOT NULL,
    FOREIGN KEY (numeroCompte) REFERENCES Client(numeroCompte) ON DELETE CASCADE
);

CREATE TABLE Retrait (
    idRetrait SERIAL PRIMARY KEY,
    numeroCompte VARCHAR(20) NOT NULL,
    numeroCheque VARCHAR(20),
    Montant_Retrait INT NOT NULL,
    DateRetrait VARCHAR(30) NOT NULL,
    FOREIGN KEY (numeroCompte) REFERENCES Client(numeroCompte) ON DELETE CASCADE
);

-- 2. Insertion de 2 clients exemple (avec adresses fictives)
INSERT INTO Client (numeroCompte, Nom, Adresse, Solde) VALUES 
('COMPTE-001', 'Alice Dupont', '10 Rue de Paris, 75001', 1000),
('COMPTE-002', 'Bob Martin', '5 Avenue de Lyon, 69002', 2400);

-- 3. Insertion de 2 versements par client
INSERT INTO Versement (numeroCompte, Montant_Versement, dateVersement) VALUES 
('COMPTE-001', 1000, '2023-10-01 10:00:00'),
('COMPTE-001', 500, '2023-10-15 14:30:00'),
('COMPTE-002', 2000, '2023-10-02 09:15:00'),
('COMPTE-002', 1000, '2023-10-20 16:45:00');

-- 4. Insertion de 2 retraits par client
INSERT INTO Retrait (numeroCompte, numeroCheque, Montant_Retrait, DateRetrait) VALUES 
('COMPTE-001', 'CHQ-A01', 200, '2023-10-10 11:00:00'),
('COMPTE-001', 'CHQ-A02', 300, '2023-10-25 15:00:00'),
('COMPTE-002', 'CHQ-B01', 500, '2023-10-05 10:30:00'),
('COMPTE-002', 'CHQ-B02', 100, '2023-10-28 08:00:00');
