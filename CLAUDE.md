Tips


Running Maven in Claude web sandbox rewuires that nstead of running `mvn` directly, use the `run-mvn.js` wrapper script:

```bash
node run-mvn.js clean install
```