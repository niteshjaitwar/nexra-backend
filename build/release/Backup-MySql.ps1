param(
    [Parameter(Mandatory = $true)]
    [string]$OutputDirectory,
    [string]$Database = $env:NEXRA_DB_NAME,
    [string]$Host = $env:NEXRA_DB_HOST,
    [string]$Port = $env:NEXRA_DB_PORT,
    [string]$Username = $env:NEXRA_DB_USER,
    [string]$Password = $env:NEXRA_DB_PASSWORD
)

$ErrorActionPreference = "Stop"

if (-not $Database) { throw "Database name is required (param -Database or env NEXRA_DB_NAME)." }
if (-not $Host) { throw "DB host is required (param -Host or env NEXRA_DB_HOST)." }
if (-not $Port) { $Port = "3306" }
if (-not $Username) { throw "DB username is required (param -Username or env NEXRA_DB_USER)." }
if (-not $Password) { throw "DB password is required (param -Password or env NEXRA_DB_PASSWORD)." }

$timestamp = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null

$sqlFile = Join-Path $OutputDirectory ("nexra-backup-{0}.sql" -f $timestamp)
$shaFile = "$sqlFile.sha256"
$metaFile = "$sqlFile.meta.json"

$env:MYSQL_PWD = $Password
try {
    & mysqldump `
        --single-transaction `
        --quick `
        --routines `
        --triggers `
        --events `
        --set-gtid-purged=OFF `
        --host=$Host `
        --port=$Port `
        --user=$Username `
        $Database | Out-File -FilePath $sqlFile -Encoding utf8
}
finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

if (-not (Test-Path $sqlFile)) {
    throw "Backup failed: SQL dump file was not created."
}

$hash = Get-FileHash -Path $sqlFile -Algorithm SHA256
"{0} *{1}" -f $hash.Hash.ToLowerInvariant(), (Split-Path -Leaf $sqlFile) | Set-Content -Path $shaFile -Encoding ascii

$meta = [ordered]@{
    generatedAtUtc = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    database = $Database
    host = $Host
    port = $Port
    backupFile = (Split-Path -Leaf $sqlFile)
    sha256File = (Split-Path -Leaf $shaFile)
    sizeBytes = (Get-Item $sqlFile).Length
}
$meta | ConvertTo-Json -Depth 4 | Set-Content -Path $metaFile -Encoding utf8

Write-Host "Backup created:"
Write-Host "  SQL:   $sqlFile"
Write-Host "  SHA:   $shaFile"
Write-Host "  META:  $metaFile"
