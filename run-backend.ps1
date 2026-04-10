param(
    [switch]$UseProdProfile
)

function Import-DotEnvFile {
    param([string]$Path)

    if (-not (Test-Path -Path $Path)) {
        return
    }

    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            return
        }

        $pair = $line -split '=', 2
        if ($pair.Count -ne 2) {
            return
        }

        $key = $pair[0].Trim()
        $value = $pair[1].Trim()

        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        if (-not (Get-Item -Path "Env:$key" -ErrorAction SilentlyContinue).Value) {
            Set-Item -Path "Env:$key" -Value $value
        }
    }

    Write-Host "Loaded environment variables from $Path"
}

Import-DotEnvFile -Path ".env"
Import-DotEnvFile -Path ".env.local"

$required = @("DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET")
$missing = @()

foreach ($name in $required) {
    if (-not (Get-Item -Path "Env:$name" -ErrorAction SilentlyContinue).Value) {
        $missing += $name
    }
}

if ($missing.Count -gt 0) {
    Write-Error "Missing environment variables: $($missing -join ', ')"
    Write-Host "Create .env from .env.example or set variables in PowerShell before running this script."
    exit 1
}

if ($UseProdProfile) {
    $env:SPRING_PROFILES_ACTIVE = "prod"
}

Write-Host "Starting Spring Boot application..."
Write-Host "DB_URL: $($env:DB_URL -replace ':[^:@/]+@', ':****@')"
Write-Host "DB_USERNAME: $env:DB_USERNAME"
Write-Host "SPRING_PROFILES_ACTIVE: $env:SPRING_PROFILES_ACTIVE"

mvn spring-boot:run
