# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this plugin, **please do not open a public GitHub issue**.

Instead, please report it responsibly by emailing the maintainers directly or
using [GitHub's private vulnerability reporting](https://github.com/grails-plugins/grails-server-timing/security/advisories/new).

### What to Include

- A description of the vulnerability
- Steps to reproduce the issue
- The potential impact
- Any suggested fixes (if you have them)

### Response Timeline

- We will acknowledge receipt within **48 hours**
- We will provide an initial assessment within **1 week**
- We will work with you to understand and resolve the issue before any public disclosure

## Security Considerations

This plugin injects `Server-Timing` headers that expose server-side timing information. By default, the plugin is *
*disabled in production** to mitigate the risk
of [timing attacks](https://w3c.github.io/server-timing/#security-considerations).

If you enable the plugin in production, be aware that:

- Timing data may help attackers infer information about server-side operations (e.g., whether a database lookup found a
  record)
- Cross-origin access to `Server-Timing` data requires the `Timing-Allow-Origin` header, which this plugin does **not**
  set automatically

See the [W3C Server Timing Security Considerations](https://w3c.github.io/server-timing/#security-considerations) for
more details.

## Supported Versions

| Version      | Supported |
|--------------|-----------|
| 0.x (latest) | Yes       |
