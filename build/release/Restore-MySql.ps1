param(
    [Parameter(Mandatory = $true)]
    [string]$BackupSqlFile,
    [string]$Database = $env:NEXRA_DB_NAME,
    [string]$Host = $env:NEXRA_DB_HOST,
    [string]$Port = $env:NEXRA_DB_PORT,
    [string]$Username = $env:NEXRA_DB_USER,
    [string]$Password = $env:NEXRA_DB_PASSWORD,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $BackupSqlFile)) { throw "Backup SQL file not found: $BackupSqlFile" }
if (-not $Database) { throw "Database name is required (param -Database or env NEXRA_DB_NAME)." }
if (-not $Host) { throw "DB host is required (param -Host or env NEXRA_DB_HOST)." }
if (-not $Port) { $Port = "3306" }
if (-not $Username) { throw "DB username is required (param -Username or env NEXRA_DB_USER)." }
if (-not $Password) { throw "DB password is required (param -Password or env NEXRA_DB_PASSWORD)." }

$shaFile = "$BackupSqlFile.sha256"
if (Test-Path $shaFile) {
    $expected = (Get-Content $shaFile).Split(" ")[0].Trim().ToLowerInvariant()
    $actual = (Get-FileHash -Path $BackupSqlFile -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($expected -ne $actual) {
        throw "Backup checksum mismatch. Expected=$expected Actual=$actual"
    }
}

if (-not $Force) {
    throw "Restore is destructive. Re-run with -Force after incident approval."
}

$env:MYSQL_PWD = $Password
try {
    Get-Content -Path $BackupSqlFile | & mysql --host=$Host --port=$Port --user=$Username $Database
}
finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Host "Restore completed from $BackupSqlFile into $Database on $Host:$Port"
