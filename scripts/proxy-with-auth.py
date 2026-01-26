#!/usr/bin/env python3
"""
HTTP Proxy with Bearer Token Authentication Handler

This proxy intercepts HTTP requests, adds Bearer token authentication,
and forwards them to the upstream proxy. Useful for tools like Maven
that don't natively support Bearer token authentication.

Usage:
    python3 proxy-with-auth.py [--port 18888] [--upstream-host 21.0.0.195] [--upstream-port 15004]
"""

import sys
import socket
import threading
import argparse
import os
import re

# Configuration
UPSTREAM_PROXY_HOST = os.getenv('PROXY_HOST', '21.0.0.195')
UPSTREAM_PROXY_PORT = os.getenv('PROXY_PORT', 15004)
LOCAL_LISTEN_PORT = os.getenv('LOCAL_PROXY_PORT', 18888)

# Extract JWT token from environment
PROXY_URL = os.getenv('HTTP_PROXY', '')
if PROXY_URL:
    # Format: http://username:jwt_TOKEN@host:port
    match = re.search(r':jwt_(.+?)@', PROXY_URL)
    JWT_TOKEN = 'eyJ0eXAiOiJKV1QiLCJhbGciOi' + match.group(1) if match else None
else:
    JWT_TOKEN = os.getenv('JWT_TOKEN')

if not JWT_TOKEN:
    print("ERROR: JWT_TOKEN not found in HTTP_PROXY environment variable or JWT_TOKEN env var")
    print("HTTP_PROXY format: http://username:jwt_TOKEN@host:port")
    sys.exit(1)

print(f"Proxy Auth Handler")
print(f"  Upstream: {UPSTREAM_PROXY_HOST}:{UPSTREAM_PROXY_PORT}")
print(f"  Local Listen: 127.0.0.1:{LOCAL_LISTEN_PORT}")
print(f"  JWT Token: {JWT_TOKEN[:30]}...")
print()


def handle_connect(client_sock, host, port):
    """Handle CONNECT requests (for HTTPS tunneling)"""
    upstream_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        upstream_sock.connect((UPSTREAM_PROXY_HOST, int(UPSTREAM_PROXY_PORT)))

        # Send CONNECT request with Bearer token
        connect_req = f"CONNECT {host}:{port} HTTP/1.1\r\n"
        connect_req += f"Host: {host}:{port}\r\n"
        connect_req += f"Proxy-Authorization: Bearer {JWT_TOKEN}\r\n"
        connect_req += "Connection: close\r\n"
        connect_req += "\r\n"

        upstream_sock.sendall(connect_req.encode())

        # Read response
        response = b""
        while True:
            chunk = upstream_sock.recv(1024)
            if not chunk:
                break
            response += chunk
            if b"\r\n\r\n" in response:
                break

        response_str = response.decode('utf-8', errors='ignore')

        if "200" in response_str.split('\n')[0]:
            # Connection established
            client_sock.sendall(b"HTTP/1.1 200 Connection Established\r\n\r\n")

            # Tunnel data bidirectionally
            tunnel_bidirectional(client_sock, upstream_sock)
        else:
            # Connection failed
            client_sock.sendall(response)

    except Exception as e:
        print(f"CONNECT error: {e}")
        client_sock.sendall(f"HTTP/1.1 500 Error\r\n\r\n{str(e)}".encode())
    finally:
        try:
            upstream_sock.close()
        except:
            pass


def handle_http(client_sock, request_data):
    """Handle HTTP requests"""
    upstream_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        upstream_sock.connect((UPSTREAM_PROXY_HOST, int(UPSTREAM_PROXY_PORT)))

        # Parse the request
        request_str = request_data.decode('utf-8', errors='ignore')
        lines = request_str.split('\r\n')

        # Add Bearer token to the request
        modified_request = ""
        for i, line in enumerate(lines):
            modified_request += line + "\r\n"
            # Add Bearer token after Host header
            if line.startswith("Host:") and i > 0:
                modified_request += f"Proxy-Authorization: Bearer {JWT_TOKEN}\r\n"

        upstream_sock.sendall(modified_request.encode())

        # Forward response
        while True:
            chunk = upstream_sock.recv(8192)
            if not chunk:
                break
            client_sock.sendall(chunk)

    except Exception as e:
        print(f"HTTP error: {e}")
        client_sock.sendall(f"HTTP/1.1 500 Error\r\n\r\n{str(e)}".encode())
    finally:
        try:
            upstream_sock.close()
        except:
            pass


def tunnel_bidirectional(sock1, sock2):
    """Tunnel data bidirectionally between two sockets"""
    def forward(src, dst):
        try:
            while True:
                data = src.recv(8192)
                if not data:
                    break
                dst.sendall(data)
        except:
            pass
        finally:
            try:
                src.close()
                dst.close()
            except:
                pass

    # Create threads for bidirectional forwarding
    t1 = threading.Thread(target=forward, args=(sock1, sock2), daemon=True)
    t2 = threading.Thread(target=forward, args=(sock2, sock1), daemon=True)

    t1.start()
    t2.start()

    t1.join()
    t2.join()


def handle_client(client_sock, addr):
    """Handle incoming client connection"""
    try:
        # Read the request line
        request_data = b""
        while True:
            chunk = client_sock.recv(4096)
            if not chunk:
                return
            request_data += chunk
            if b"\r\n\r\n" in request_data or len(request_data) > 10000:
                break

        request_str = request_data.decode('utf-8', errors='ignore')
        request_line = request_str.split('\r\n')[0]

        if request_line.startswith('CONNECT'):
            # HTTPS tunneling
            parts = request_line.split()
            host_port = parts[1]
            host, port = host_port.split(':')
            handle_connect(client_sock, host, port)
        else:
            # HTTP request
            handle_http(client_sock, request_data)

    except Exception as e:
        print(f"Error handling client {addr}: {e}")
    finally:
        try:
            client_sock.close()
        except:
            pass


def main():
    parser = argparse.ArgumentParser(description='HTTP Proxy with Bearer Token Authentication')
    parser.add_argument('--port', type=int, default=LOCAL_LISTEN_PORT, help='Local listen port')
    parser.add_argument('--upstream-host', default=UPSTREAM_PROXY_HOST, help='Upstream proxy host')
    parser.add_argument('--upstream-port', type=int, default=int(UPSTREAM_PROXY_PORT), help='Upstream proxy port')

    args = parser.parse_args()

    # Create server socket
    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_sock.bind(('127.0.0.1', args.port))
    server_sock.listen(5)

    print(f"✓ Proxy listening on 127.0.0.1:{args.port}")
    print(f"✓ Forwarding to {args.upstream_host}:{args.upstream_port}")
    print()

    try:
        while True:
            client_sock, addr = server_sock.accept()
            thread = threading.Thread(
                target=handle_client,
                args=(client_sock, addr),
                daemon=True
            )
            thread.start()
    except KeyboardInterrupt:
        print("\nShutting down...")
    finally:
        server_sock.close()


if __name__ == '__main__':
    main()
