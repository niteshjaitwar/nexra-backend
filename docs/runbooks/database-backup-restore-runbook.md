# Database Backup And Restore Runbook (MySQL)

## Purpose

Provide a repeatable backup and restore process with evidence artifacts for launch gates.

## Preconditions

- Incident commander approval for restore.
- `mysqldump` and `mysql` CLI installed on execution host.
- Database credentials exported as environment variables:
  - `NEXRA_DB_HOST`
  - `NEXRA_DB_PORT` (default `3306`)
  - `NEXRA_DB_NAME`
  - `NEXRA_DB_USER`
  - `NEXRA_DB_PASSWORD`

## Backup Procedure

From repository root:

```powershell
.\build\release\Backup-MySql.ps1 -OutputDirectory .\build\release\db-backups
```

Expected outputs:

- `nexra-backup-<timestamp>.sql`
- `nexra-backup-<timestamp>.sql.sha256`
- `nexra-backup-<timestamp>.sql.meta.json`

## Backup Validation

1. Confirm SQL file exists and size is non-zero.
2. Confirm SHA256 file exists.
3. Confirm metadata file exists with DB host, DB name, timestamp.
4. Store all three files in encrypted backup storage.

## Restore Procedure

Restore is destructive. Use approved maintenance window only.

```powershell
.\build\release\Restore-MySql.ps1 -BackupSqlFile .\build\release\db-backups\nexra-backup-<timestamp>.sql -Force
```

Restore performs checksum validation when `.sha256` file is present.

## Post-Restore Validation

1. Start backend with `prod` profile in isolated environment.
2. Verify Flyway schema version:
   - latest expected: `v43`.
3. Execute smoke checks:
   - auth login
   - employee summary
   - payroll status
   - CRM leads list
4. Capture command outputs and attach to release evidence pack.

## Evidence To Capture

- Backup command output
- Restore command output
- File list with sizes and checksums
- Post-restore smoke test result
- Incident/change ticket reference
