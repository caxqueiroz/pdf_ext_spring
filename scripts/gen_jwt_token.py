import argparse
import datetime
import jwt
import os
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization


def load_private_key(file_path):
    with open(file_path, "rb") as key_file:
        private_key = serialization.load_pem_private_key(
            key_file.read(),
            password=None,
            backend=default_backend()
        )
    return private_key


def generate_token(private_key):
    payload = {
        'iss': 'cax',
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=12)
    }
    token = jwt.encode(payload, private_key, algorithm='RS256')
    return token


parser = argparse.ArgumentParser(description='Generate JWT token.')
parser.add_argument('--key', type=str, help='Path to the private key file')
args = parser.parse_args()
pkey_path = args.key if args.key else os.getenv('PRIVATE_KEY_PATH', './scripts/private_key.pem')
pkey = load_private_key(pkey_path)
token = generate_token(pkey)
print(token)
