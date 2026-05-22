param(
    [string]$ImageName = "nexra",
    [string]$ImageTag = "local-dev",
    [string]$OutputDir = "build/release/container"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI is not installed or not available in PATH."
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

$imageRef = "$ImageName`:$ImageTag"
$timestampUtc = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")

docker build --pull --no-cache --file Dockerfile --tag $imageRef .
if ($LASTEXITCODE -ne 0) {
    throw "Docker build failed. Ensure Docker daemon is running and try again."
}

$inspectJson = docker image inspect $imageRef
if ($LASTEXITCODE -ne 0 -or -not $inspectJson) {
    throw "Docker image inspect failed for image $imageRef."
}
$inspect = $inspectJson | ConvertFrom-Json
$imageId = "$($inspect[0].Id)".Trim()
$repoDigests = $inspect[0].RepoDigests
$repoDigest = if ($repoDigests) { ($repoDigests -join ",").Trim() } else { "N/A (not pushed)" }

$reportFile = Join-Path $OutputDir "reproducible-image-evidence.md"

@"
# Reproducible Image Build Evidence

- Generated at (UTC): $timestampUtc
- Dockerfile: Dockerfile
- Image reference: $imageRef
- Image ID: $imageId
- Repo digest(s): $repoDigest

## Build Command

docker build --pull --no-cache --file Dockerfile --tag $imageRef .
"@ | Set-Content -Path $reportFile -Encoding UTF8

Write-Host "Generated image build evidence file: $reportFile"
