#!/usr/bin/env python3
"""
Simple HTTP Proxy with Bearer Token Authentication

This proxy:
1. Listens on localhost:18888
2. Intercepts all requests
3. Adds Bearer token authentication header
4. Forwards to upstream proxy at 21.0.0.195:15004
5. Returns responses to client

No complex bidirectional tunneling - just request/response forwarding.
"""

import socket
import sys
import os
import re
import select

# Configuration from environment
PROXY_URL = os.getenv('HTTP_PROXY', '')
LISTEN_HOST = '127.0.0.1'
LISTEN_PORT = int(os.getenv('LOCAL_PROXY_PORT', 18888))
UPSTREAM_HOST = os.getenv('PROXY_HOST', '21.0.0.195')
UPSTREAM_PORT = int(os.getenv('PROXY_PORT', 15004))

# Extract JWT token
JWT_TOKEN = None
if PROXY_URL:
    # Format: http://username:jwt_TOKEN@host:port
    # Extract everything after "jwt_" and before "@"
    match = re.search(r'jwt_([^@]+)', PROXY_URL)
    if match:
        JWT_TOKEN = match.group(1)

if not JWT_TOKEN:
    print("ERROR: Could not extract JWT token from HTTP_PROXY environment variable")
    print(f"HTTP_PROXY={PROXY_URL[:50]}...")
    sys.exit(1)

print("=" * 60)
print("HTTP Proxy with Bearer Token Authentication")
print("=" * 60)
print(f"Listen:  {LISTEN_HOST}:{LISTEN_PORT}")
print(f"Upstream: {UPSTREAM_HOST}:{UPSTREAM_PORT}")
print(f"JWT Token: {JWT_TOKEN[:40]}...")
print("=" * 60)
print()


def forward_data(src_sock, dst_sock, is_request=True):
    """Forward data from source to destination socket"""
    try:
        data = b""
        while True:
            chunk = src_sock.recv(4096)
            if not chunk:
                break
            data += chunk

            # For requests, add Bearer token before forwarding
            if is_request and len(data) > 0:
                data_str = data.decode('utf-8', errors='ignore')
                # Add Bearer token if not already present
                if 'Proxy-Authorization' not in data_str:
                    # Insert after Host header
                    lines = data_str.split('\r\n')
                    new_lines = []
                    for i, line in enumerate(lines):
                        new_lines.append(line)
                        if line.startswith('Host:'):
                            new_lines.append(f'Proxy-Authorization: Bearer {JWT_TOKEN}')
                    data = '\r\n'.join(new_lines).encode()

                dst_sock.sendall(data)
                is_request = False  # Only modify first request part
                data = b""

        if data:
            dst_sock.sendall(data)

    except Exception as e:
        print(f"Forward error: {e}")


def handle_client(client_sock, addr):
    """Handle a single client connection"""
    try:
        # Connect to upstream proxy
        upstream_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        upstream_sock.settimeout(10)
        upstream_sock.connect((UPSTREAM_HOST, UPSTREAM_PORT))

        print(f"[{addr[0]}:{addr[1]}] Connected to upstream proxy")

        # Set sockets to non-blocking
        client_sock.setblocking(False)
        upstream_sock.setblocking(False)

        client_to_upstream = b""
        upstream_to_client = b""
        upstream_connected = True

        while True:
            # Use select to monitor both sockets
            readable, _, _ = select.select(
                [client_sock, upstream_sock],
                [],
                [client_sock, upstream_sock],
                timeout=5
            )

            # Check for errors
            if not readable and upstream_connected:
                # Timeout - check if connection is still alive
                break

            for sock in readable:
                try:
                    data = sock.recv(4096)
                    if not data:
                        upstream_connected = False
                        break

                    if sock == client_sock:
                        # Data from client - add Bearer token and send to upstream
                        data_str = data.decode('utf-8', errors='ignore')

                        # Add Bearer token for first request
                        if not upstream_to_client and 'Proxy-Authorization' not in data_str:
                            lines = data_str.split('\r\n')
                            new_lines = []
                            for i, line in enumerate(lines):
                                new_lines.append(line)
                                if line.startswith('Host:') or (i == 0 and line.startswith('CONNECT')):
                                    if 'Proxy-Authorization' not in lines[i+1] if i+1 < len(lines) else True:
                                        new_lines.append(f'Proxy-Authorization: Bearer {JWT_TOKEN}')
                            data = '\r\n'.join(new_lines).encode()

                        upstream_sock.sendall(data)

                    else:  # Data from upstream
                        # Send to client
                        client_sock.sendall(data)
                        upstream_to_client += data

                except socket.error as e:
                    if e.errno not in (11, 35):  # EAGAIN, EWOULDBLOCK
                        raise

            if not upstream_connected:
                break

    except socket.timeout:
        print(f"[{addr[0]}:{addr[1]}] Socket timeout")
    except Exception as e:
        print(f"[{addr[0]}:{addr[1]}] Error: {e}")
    finally:
        try:
            upstream_sock.close()
        except:
            pass
        try:
            client_sock.close()
        except:
            pass
        print(f"[{addr[0]}:{addr[1]}] Connection closed")


def main():
    """Main server loop"""
    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        server_sock.bind((LISTEN_HOST, LISTEN_PORT))
        server_sock.listen(5)
        print(f"✓ Proxy listening on {LISTEN_HOST}:{LISTEN_PORT}")
        print()

        while True:
            try:
                client_sock, addr = server_sock.accept()
                print(f"[{addr[0]}:{addr[1]}] New connection")
                handle_client(client_sock, addr)
            except KeyboardInterrupt:
                break

    except Exception as e:
        print(f"Server error: {e}")
    finally:
        server_sock.close()
        print("Server stopped")


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print("\nShutting down...")
        sys.exit(0)
