# -*- coding: utf-8 -*-
from flask import Flask
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

from .routes.client_routes import client_bp
from .routes.tx_routes import tx_bp
from .routes.auth_routes import auth_bp
from .auth_utils import require_auth

# Mapping pour coller exactement à la demande
app.register_blueprint(client_bp, url_prefix='/client')
app.register_blueprint(tx_bp) # Préfixe retiré, on gère les routes complètes dans tx_routes
app.register_blueprint(auth_bp)

@app.route('/')
def index():
    return {"message": "Serveur REST operationnel. Endpoints : /client et /transactions"}

@app.route('/etat/clients', methods=['GET'])
@require_auth
def etat_clients():
    from flask import jsonify
    from app.models import ClientModel
    etats = ClientModel.get_etat_clients()
    return jsonify(etats), 200
