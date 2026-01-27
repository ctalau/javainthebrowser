# Maven Proxy Setup for Restricted Environments

## Running Maven Commands

Running Maven in Claude web sandbox requires that instead of running `mvn` directly, use the `run-mvn.js` wrapper script:

```bash
# Standard Maven commands like
node run-mvn.js clean install
```

## Troubleshooting

**Proxy won't start - port 8080 in use**
```bash
# Kill any processes using port 8080
lsof -ti:8080 | xargs kill -9
```