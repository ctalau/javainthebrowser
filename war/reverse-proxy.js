(() => {
  const BASE_URL = "/api/cors/myapp.com/path";
  const statusNode = document.getElementById("proxy-status");

  const setStatus = (message) => {
    if (statusNode) {
      statusNode.textContent = message;
    }
  };

  const ensureHead = (doc) => {
    if (doc.head) {
      return doc.head;
    }
    const head = doc.createElement("head");
    doc.documentElement.insertBefore(head, doc.documentElement.firstChild);
    return head;
  };

  const ensureBody = (doc) => {
    if (doc.body) {
      return doc.body;
    }
    const body = doc.createElement("body");
    doc.documentElement.appendChild(body);
    return body;
  };

  const setBaseUrl = (doc) => {
    const head = ensureHead(doc);
    let base = head.querySelector("base");
    if (!base) {
      base = doc.createElement("base");
      head.insertBefore(base, head.firstChild);
    }
    base.setAttribute("href", BASE_URL);
  };

  const injectDiagnostics = (doc) => {
    const body = ensureBody(doc);
    const script = doc.createElement("script");
    script.src = "/diagnostics.js";
    script.defer = true;
    script.dataset.proxyBaseUrl = BASE_URL;
    body.appendChild(script);
  };

  const serializeDoctype = (doc) => {
    if (!doc.doctype) {
      return "<!doctype html>";
    }
    const { name, publicId, systemId } = doc.doctype;
    const publicSegment = publicId ? ` PUBLIC \"${publicId}\"` : "";
    const systemSegment = systemId ? ` \"${systemId}\"` : "";
    return `<!doctype ${name}${publicSegment}${systemSegment}>`;
  };

  const rewritePage = (html) => {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, "text/html");
    if (!doc.documentElement) {
      throw new Error("Unable to parse proxied HTML.");
    }
    setBaseUrl(doc);
    injectDiagnostics(doc);
    const doctype = serializeDoctype(doc);
    const markup = `${doctype}\n${doc.documentElement.outerHTML}`;
    document.open();
    document.write(markup);
    document.close();
  };

  const loadProxy = async () => {
    try {
      const response = await fetch(BASE_URL, { credentials: "include" });
      if (!response.ok) {
        throw new Error(`Proxy request failed (${response.status}).`);
      }
      const html = await response.text();
      rewritePage(html);
    } catch (error) {
      setStatus(`Failed to load proxied site: ${error.message}`);
    }
  };

  loadProxy();
})();
