# -*- coding: utf-8 -*-
from flask import Blueprint, request, jsonify
from app.models import UserModel
from app.auth_utils import generate_token

auth_bp = Blueprint('auth_bp', __name__)


@auth_bp.route('/auth/register', methods=['POST'])
def register():
    data = request.json or {}
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"error": "Nom d'utilisateur et mot de passe requis"}), 400

    if UserModel.find_by_username(username):
        return jsonify({"error": "Nom d'utilisateur deja utilise"}), 409

    user_id = UserModel.create_user(username, password)
    if not user_id:
        return jsonify({"error": "Creation impossible"}), 500

    return jsonify({"message": "Utilisateur cree"}), 201


@auth_bp.route('/auth/login', methods=['POST'])
def login():
    data = request.json or {}
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"error": "Nom d'utilisateur et mot de passe requis"}), 400

    user = UserModel.find_by_username(username)
    if not user or not UserModel.verify_password(password, user['password_hash']):
        return jsonify({"error": "Identifiants invalides"}), 401

    token = generate_token(user['id'], user['username'])
    return jsonify({"token": token, "username": user['username']}), 200
