#!/usr/bin/env python3
from pathlib import Path
import re
import sys

CHUNK = 8192

if len(sys.argv) != 3:
    print("usage: patch_filesystem_content.py <input> <output>")
    sys.exit(2)

text = Path(sys.argv[1]).read_text()
pattern = re.compile(r'files\.put\("([^"]+)",\s*"([^"]*)"\);', re.S)

count = 0

def chunk_string(raw: str) -> str:
    if len(raw) <= CHUNK:
        return f'"{raw}"'
    parts = [raw[i:i+CHUNK] for i in range(0, len(raw), CHUNK)]
    expr = ["new StringBuilder()"]
    expr.extend(f'.append("{p}")' for p in parts)
    expr.append('.toString()')
    return "\n        " + "\n        ".join(expr)

def repl(m: re.Match) -> str:
    global count
    count += 1
    return f'files.put("{m.group(1)}", {chunk_string(m.group(2))});'

patched = pattern.sub(repl, text)
Path(sys.argv[2]).write_text(patched)
print(f"patched entries: {count}")
