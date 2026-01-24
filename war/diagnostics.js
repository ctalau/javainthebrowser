(() => {
  if (window.__proxyDiagnostics) {
    return;
  }

  const state = {
    entries: [],
    maxEntries: 200,
  };

  const diagnostics = {
    addEntry(type, message, detail) {
      const entry = {
        time: new Date(),
        type,
        message,
        detail,
      };
      state.entries.push(entry);
      if (state.entries.length > state.maxEntries) {
        state.entries.shift();
      }
      renderEntry(entry);
    },
  };

  const createPanel = () => {
    const panel = document.createElement("div");
    panel.id = "proxy-diagnostics";
    panel.innerHTML = `
      <div class="proxy-diagnostics__header">
        <strong>Diagnostics</strong>
        <button type="button" class="proxy-diagnostics__clear">Clear</button>
      </div>
      <div class="proxy-diagnostics__body"></div>
    `;

    const style = document.createElement("style");
    style.textContent = `
      #proxy-diagnostics {
        position: fixed;
        right: 16px;
        bottom: 16px;
        width: 360px;
        max-height: 50vh;
        display: flex;
        flex-direction: column;
        font-family: Arial, sans-serif;
        font-size: 12px;
        color: #1f1f1f;
        background: rgba(255, 255, 255, 0.95);
        border: 1px solid #d0d0d0;
        border-radius: 8px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        z-index: 2147483647;
      }
      .proxy-diagnostics__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 10px;
        border-bottom: 1px solid #e4e4e4;
        background: #f6f6f6;
      }
      .proxy-diagnostics__clear {
        font-size: 11px;
        border: 1px solid #ccc;
        border-radius: 4px;
        background: #fff;
        padding: 2px 6px;
        cursor: pointer;
      }
      .proxy-diagnostics__body {
        padding: 8px 10px;
        overflow-y: auto;
        flex: 1;
      }
      .proxy-diagnostics__entry {
        margin-bottom: 6px;
        padding-bottom: 6px;
        border-bottom: 1px solid #f0f0f0;
      }
      .proxy-diagnostics__entry:last-child {
        border-bottom: none;
        margin-bottom: 0;
      }
      .proxy-diagnostics__meta {
        color: #666;
        font-size: 10px;
        margin-bottom: 2px;
      }
      .proxy-diagnostics__type {
        font-weight: bold;
        margin-right: 4px;
      }
      .proxy-diagnostics__detail {
        margin-top: 4px;
        white-space: pre-wrap;
        color: #333;
      }
    `;

    document.head.appendChild(style);
    document.body.appendChild(panel);

    const clearButton = panel.querySelector(".proxy-diagnostics__clear");
    clearButton.addEventListener("click", () => {
      state.entries = [];
      panel.querySelector(".proxy-diagnostics__body").innerHTML = "";
    });

    return panel;
  };

  let panelInstance;
  const ensurePanel = () => {
    if (!panelInstance) {
      panelInstance = createPanel();
    }
    return panelInstance;
  };

  const formatArgs = (args) =>
    args
      .map((arg) => {
        if (typeof arg === "string") {
          return arg;
        }
        try {
          return JSON.stringify(arg);
        } catch (error) {
          return String(arg);
        }
      })
      .join(" ");

  const renderEntry = (entry) => {
    const panel = ensurePanel();
    const body = panel.querySelector(".proxy-diagnostics__body");
    const row = document.createElement("div");
    row.className = "proxy-diagnostics__entry";
    row.innerHTML = `
      <div class="proxy-diagnostics__meta">
        ${entry.time.toLocaleTimeString()}
        <span class="proxy-diagnostics__type">${entry.type}</span>
      </div>
      <div>${entry.message}</div>
    `;
    if (entry.detail) {
      const detail = document.createElement("div");
      detail.className = "proxy-diagnostics__detail";
      detail.textContent = entry.detail;
      row.appendChild(detail);
    }
    body.appendChild(row);
    body.scrollTop = body.scrollHeight;
  };

  const wrapConsole = () => {
    const original = {
      log: console.log,
      info: console.info,
      warn: console.warn,
      error: console.error,
    };

    Object.keys(original).forEach((level) => {
      console[level] = (...args) => {
        diagnostics.addEntry(`console.${level}`, formatArgs(args));
        original[level].apply(console, args);
      };
    });
  };

  const wrapFetch = () => {
    if (!window.fetch) {
      return;
    }
    const originalFetch = window.fetch.bind(window);
    window.fetch = (...args) => {
      const request = args[0];
      const init = args[1];
      const url = typeof request === "string" ? request : request.url;
      const method = (init && init.method) || (request && request.method) || "GET";
      const start = performance.now();
      diagnostics.addEntry("network.request", `${method} ${url}`);
      return originalFetch(...args)
        .then((response) => {
          const duration = Math.round(performance.now() - start);
          diagnostics.addEntry(
            "network.response",
            `${method} ${url} → ${response.status} (${duration}ms)`
          );
          return response;
        })
        .catch((error) => {
          diagnostics.addEntry("network.error", `${method} ${url}`, error.message);
          throw error;
        });
    };
  };

  const wrapXhr = () => {
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function (method, url, ...rest) {
      this.__diagnostics = { method, url };
      return originalOpen.call(this, method, url, ...rest);
    };

    XMLHttpRequest.prototype.send = function (...args) {
      if (this.__diagnostics) {
        const { method, url } = this.__diagnostics;
        const start = performance.now();
        diagnostics.addEntry("network.request", `${method} ${url}`);
        this.addEventListener("loadend", () => {
          const duration = Math.round(performance.now() - start);
          diagnostics.addEntry(
            "network.response",
            `${method} ${url} → ${this.status} (${duration}ms)`
          );
        });
      }
      return originalSend.apply(this, args);
    };
  };

  const registerErrors = () => {
    window.addEventListener("error", (event) => {
      diagnostics.addEntry(
        "error",
        event.message || "Uncaught error",
        event.error ? event.error.stack : ""
      );
    });

    window.addEventListener("unhandledrejection", (event) => {
      const reason = event.reason instanceof Error ? event.reason.message : String(event.reason);
      diagnostics.addEntry("error", "Unhandled promise rejection", reason);
    });
  };

  const start = () => {
    ensurePanel();
    wrapConsole();
    wrapFetch();
    wrapXhr();
    registerErrors();
    diagnostics.addEntry("diagnostics", "Diagnostics panel initialized.");
  };

  window.__proxyDiagnostics = diagnostics;

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start);
  } else {
    start();
  }
})();
