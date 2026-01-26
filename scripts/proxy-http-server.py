#!/usr/bin/env python3
"""
HTTP Proxy Server with Bearer Token Authentication

Uses Python's built-in http.server for reliability.
Intercepts requests and adds Bearer token authentication.
"""

import http.server
import socketserver
import os
import re
import socket
import urllib.request
import urllib.error

# Configuration
PROXY_URL = os.getenv('HTTP_PROXY', '')
LOCAL_PORT = int(os.getenv('LOCAL_PROXY_PORT', 18888))
UPSTREAM_HOST = os.getenv('PROXY_HOST', '21.0.0.195')
UPSTREAM_PORT = int(os.getenv('PROXY_PORT', 15004))

# Extract JWT token
JWT_TOKEN = None
if PROXY_URL:
    match = re.search(r'jwt_([^@]+)', PROXY_URL)
    if match:
        JWT_TOKEN = match.group(1)

if not JWT_TOKEN:
    print("ERROR: Could not extract JWT token from HTTP_PROXY")
    exit(1)

print("=" * 60)
print("HTTP Proxy Server with Bearer Token Authentication")
print("=" * 60)
print(f"Listening on: 127.0.0.1:{LOCAL_PORT}")
print(f"Upstream proxy: {UPSTREAM_HOST}:{UPSTREAM_PORT}")
print(f"JWT Token: {JWT_TOKEN[:40]}...")
print("=" * 60)
print()


class ProxyHandler(http.server.BaseHTTPRequestHandler):
    """HTTP request handler that adds Bearer token authentication"""

    def do_GET(self):
        self.handle_request()

    def do_POST(self):
        self.handle_request()

    def do_HEAD(self):
        self.handle_request()

    def do_PUT(self):
        self.handle_request()

    def do_DELETE(self):
        self.handle_request()

    def do_CONNECT(self):
        """Handle HTTPS CONNECT tunneling"""
        # Parse the host:port
        host_port = self.path
        try:
            host, port = host_port.split(':')
            port = int(port)
        except:
            self.send_error(400, "Invalid CONNECT target")
            return

        # Connect to upstream proxy
        try:
            upstream_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            upstream_sock.connect((UPSTREAM_HOST, UPSTREAM_PORT))

            # Send CONNECT request with Bearer token
            connect_request = f"CONNECT {host}:{port} HTTP/1.1\r\n"
            connect_request += f"Host: {host}:{port}\r\n"
            connect_request += f"Proxy-Authorization: Bearer {JWT_TOKEN}\r\n"
            connect_request += "Connection: close\r\n"
            connect_request += "\r\n"

            upstream_sock.send(connect_request.encode())

            # Read response headers
            response_line = b""
            while b"\r\n\r\n" not in response_line:
                chunk = upstream_sock.recv(1024)
                if not chunk:
                    break
                response_line += chunk

            response_str = response_line.decode('utf-8', errors='ignore')

            if "200" in response_str.split('\n')[0]:
                # Success - send 200 to client
                self.send_response(200, "Connection established")
                self.end_headers()

                # Tunnel data between client and upstream
                self.tunnel_traffic(upstream_sock)
            else:
                # Failed
                self.send_error(502, "Bad Gateway")
                response_line_str = response_line.decode('utf-8', errors='ignore').split('\n')[0]
                print(f"CONNECT failed: {response_line_str}")

        except Exception as e:
            self.send_error(502, f"Bad Gateway: {str(e)}")
            print(f"CONNECT error: {e}")

    def handle_request(self):
        """Handle HTTP requests"""
        try:
            # Get the full request
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else b""

            # Prepare the request
            request_line = f"{self.command} {self.path} {self.request_version}\r\n"
            headers = ""
            for header, value in self.headers.items():
                headers += f"{header}: {value}\r\n"

            # Add Bearer token for proxy authentication
            if "Proxy-Authorization" not in headers:
                headers += f"Proxy-Authorization: Bearer {JWT_TOKEN}\r\n"

            full_request = request_line + headers + "\r\n"

            # Connect to upstream and send request
            upstream_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            upstream_sock.connect((UPSTREAM_HOST, UPSTREAM_PORT))
            upstream_sock.send(full_request.encode() + body)

            # Read response
            response = b""
            while True:
                try:
                    chunk = upstream_sock.recv(4096)
                    if not chunk:
                        break
                    response += chunk
                except:
                    break

            upstream_sock.close()

            # Parse and forward response
            response_str = response.decode('utf-8', errors='ignore')
            parts = response_str.split('\r\n\r\n', 1)

            if len(parts) == 2:
                headers_section, body_section = parts
                headers_lines = headers_section.split('\r\n')

                # Parse status line
                status_line = headers_lines[0]
                parts = status_line.split(' ', 2)
                status = int(parts[1]) if len(parts) > 1 else 502
                message = parts[2] if len(parts) > 2 else "Bad Gateway"

                self.send_response(status, message)

                # Forward headers
                for header_line in headers_lines[1:]:
                    if header_line and ':' in header_line:
                        key, value = header_line.split(':', 1)
                        self.send_header(key.strip(), value.strip())

                self.end_headers()

                # Send body
                if body_section:
                    self.wfile.write(body_section.encode() if isinstance(body_section, str) else body_section)
            else:
                self.send_error(502, "Bad Gateway")

        except Exception as e:
            print(f"Request error: {e}")
            self.send_error(502, f"Bad Gateway: {str(e)}")

    def tunnel_traffic(self, upstream_sock):
        """Tunnel traffic between client and upstream socket"""
        import select

        client_sock = self.connection
        try:
            while True:
                readable, _, _ = select.select([client_sock, upstream_sock], [], [], 5)
                if not readable:
                    break

                for sock in readable:
                    try:
                        data = sock.recv(4096)
                        if not data:
                            return
                        if sock == client_sock:
                            upstream_sock.send(data)
                        else:
                            client_sock.send(data)
                    except:
                        return
        finally:
            try:
                upstream_sock.close()
            except:
                pass

    def log_message(self, format, *args):
        """Override to customize logging"""
        print(f"[{self.client_address[0]}:{self.client_address[1]}] {format % args}")


if __name__ == '__main__':
    try:
        handler = ProxyHandler
        with socketserver.ThreadingTCPServer(("127.0.0.1", LOCAL_PORT), handler) as httpd:
            print(f"✓ Proxy server started")
            print()
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down...")
    except Exception as e:
        print(f"Error: {e}")
        exit(1)
