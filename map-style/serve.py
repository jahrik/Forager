"""Serves this folder at http://localhost:8765 for the preview page and for Maputnik (dispatch
2026-09-28-485). Python's own static server, plus the CORS headers Maputnik's hosted editor needs to
read the style, fonts and icons from here. Local only: it listens on 127.0.0.1. Stop it with Ctrl+C.

    python3 serve.py
"""
import functools
import http.server
import os

PORT = 8765
HERE = os.path.dirname(os.path.abspath(__file__))


class Handler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "*")
        # Chrome asks this before a public HTTPS page (the hosted Maputnik) may read from localhost.
        self.send_header("Access-Control-Allow-Private-Network", "true")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(204)
        self.end_headers()


if __name__ == "__main__":
    server = http.server.ThreadingHTTPServer(("127.0.0.1", PORT), functools.partial(Handler, directory=HERE))
    print(f"Serving {HERE} at http://localhost:{PORT}/  (preview: http://localhost:{PORT}/preview/)")
    server.serve_forever()
