# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within this project (particularly relating to Credential Manager, token storage, or Firebase Auth handling), please follow these guidelines:

1. **Do not create a public GitHub issue.**
2. Send an email to the repository maintainers or use GitHub's private vulnerability reporting feature under **Security > Advisories > Report a vulnerability**.
3. Include:
   - Type of issue (e.g., token leakage, improper session invalidation, backstack exposure).
   - Detailed step-by-step reproduction instructions.
   - Any proof of concept code or logs.
4. We will acknowledge receipt of your report within 48 hours and coordinate remediation.

## Best Practices
- Never commit private production keystores or production API keys to git.
- Ensure `google-services.json` used for production deployment contains restricted API keys.
- Always configure SHA-1 fingerprints correctly in the Google Cloud Console.
