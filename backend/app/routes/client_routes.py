# -*- coding: utf-8 -*-
from flask import Blueprint, request, jsonify
from app.models import ClientModel
from app.auth_utils import require_auth

client_bp = Blueprint('client_bp', __name__)

@client_bp.route('/ajout', methods=['POST'])
@require_auth
def add_client():
    data = request.json or {}
    numeroCompte = data.get('numeroCompte')
    nom = data.get('nom')
    adresse = data.get('adresse', 'Non renseigné')
    solde = data.get('solde', 0)
    
    if not numeroCompte or not nom:
        return jsonify({"error": "Le numero de compte et le nom sont requis"}), 400
        
    if ClientModel.create(numeroCompte, nom, adresse, solde):
        return jsonify({"message": "Client enregistre"}), 201
    else:
        return jsonify({"error": "Ajout impossible (compte existant ?)"}), 400

@client_bp.route('/supprimer', methods=['POST'])
@require_auth
def delete_client():
    compte = request.args.get('compte')
    if not compte:
        return jsonify({"error": "Le parametre '?compte=' est obligatoire"}), 400
        
    if ClientModel.delete(compte):
        return jsonify({"message": "Client supprime"}), 200
    else:
        return jsonify({"error": "Client introuvable ou suppression impossible"}), 404

@client_bp.route('/modifier', methods=['POST'])
@require_auth
def update_client():
    data = request.json or {}
    numeroCompte = data.get('numeroCompte')
    if not numeroCompte:
        return jsonify({"error": "Le numero de compte est requis pour la modification"}), 400
        
    nom = data.get('nom')
    adresse = data.get('adresse')
    solde = data.get('solde')
    
    if ClientModel.update(numeroCompte, nom, adresse, solde):
        return jsonify({"message": "Client mis a jour"}), 200
    else:
        return jsonify({"error": "Client introuvable ou modification impossible"}), 404

@client_bp.route('/recherche', methods=['GET'])
@require_auth
def search_clients():
    q = request.args.get('q', '')
    clients = ClientModel.search(q)
    return jsonify(clients), 200
