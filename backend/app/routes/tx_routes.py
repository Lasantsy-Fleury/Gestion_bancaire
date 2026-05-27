# -*- coding: utf-8 -*-
from flask import Blueprint, request, jsonify
from app.models import TransactionModel
from app.auth_utils import require_auth
from datetime import datetime

tx_bp = Blueprint('tx_bp', __name__)

@tx_bp.route('/versement/ajouter', methods=['POST'])
@require_auth
def ajouter_versement():
    data = request.json or {}
    numCompte = data.get('numCompte')
    montant = data.get('montant')
    date = data.get('date')
    
    if not all([numCompte, montant, date]):
        return jsonify({"error": "Parametres manquants : numCompte, montant ou date"}), 400

    try:
        montant_val = float(montant)
    except (TypeError, ValueError):
        return jsonify({"error": "Montant invalide"}), 400

    if montant_val <= 0:
        return jsonify({"error": "Le montant doit etre superieur a zero"}), 400
        
    if TransactionModel.versement(numCompte, montant_val, date):
        return jsonify({"message": "Versement enregistre. Solde recalcule."}), 200
    else:
        return jsonify({"error": "Versement refuse"}), 400

@tx_bp.route('/retrait/ajouter', methods=['POST'])
@require_auth
def ajouter_retrait():
    data = request.json or {}
    numCompte = data.get('numCompte')
    numCheque = data.get('numCheque', '')
    montant = data.get('montant')
    date = data.get('date')
    
    if not all([numCompte, montant, date]):
        return jsonify({"error": "Parametres manquants : numCompte, montant ou date"}), 400

    try:
        montant_val = float(montant)
    except (TypeError, ValueError):
        return jsonify({"error": "Montant invalide"}), 400

    if montant_val <= 0:
        return jsonify({"error": "Le montant doit etre superieur a zero"}), 400
        
    if TransactionModel.retrait(numCompte, numCheque, montant_val, date):
        return jsonify({"message": "Retrait enregistre. Solde recalcule."}), 200
    else:
        return jsonify({"error": "Retrait refuse ou solde insuffisant"}), 400

@tx_bp.route('/transfert', methods=['POST'])
@require_auth
def effectuer_transfert():
    data = request.json or {}
    compteSource = data.get('compteSource')
    compteDestination = data.get('compteDestination')
    montant = data.get('montant')
    
    # Si la date n'est pas fournie par le client, on prend celle du serveur
    date_tx = data.get('date', datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    
    if not all([compteSource, compteDestination, montant]):
        return jsonify({"error": "Parametres manquants : compteSource, compteDestination ou montant"}), 400

    try:
        montant_val = float(montant)
    except (TypeError, ValueError):
        return jsonify({"error": "Montant invalide"}), 400

    if montant_val <= 0:
        return jsonify({"error": "Le montant doit etre superieur a zero"}), 400
        
    if compteSource == compteDestination:
        return jsonify({"error": "Les comptes source et destination doivent etre differents"}), 400
        
    success, message = TransactionModel.transfert(compteSource, compteDestination, montant_val, date_tx)
    if success:
        return jsonify({"message": message}), 200
    else:
        return jsonify({"error": message}), 400

@tx_bp.route('/mouvements', methods=['GET'])
@require_auth
def get_mouvements():
    compte = request.args.get('compte')
    if not compte:
        return jsonify({"error": "Le parametre '?compte=' est obligatoire"}), 400
        
    mouvements = TransactionModel.get_mouvements(compte)
    return jsonify(mouvements), 200
