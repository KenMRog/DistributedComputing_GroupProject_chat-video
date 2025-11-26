#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Run backend tests using Maven in Docker

.DESCRIPTION
    This script runs the test suite for the screenshare backend application.
    Tests run inside a Docker container with Maven and JDK 17.
    Optionally loads Azure credentials from .env file for integration tests.

.PARAMETER TestType
    Type of tests to run:
    - "unit" (default): Run only unit tests (fast, no Azure required)
    - "integration": Run only integration tests (requires Azure resources)
    - "all": Run all tests (unit + integration)

.PARAMETER LoadEnv
    Load environment variables from .env file (for integration tests)

.EXAMPLE
    .\run-tests.ps1
    Run unit tests only (default)

.EXAMPLE
    .\run-tests.ps1 -TestType integration -LoadEnv
    Run integration tests with Azure credentials from .env

.EXAMPLE
    .\run-tests.ps1 -TestType all -LoadEnv
    Run all tests with Azure credentials
#>

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("unit", "integration", "all")]
    [string]$TestType = "unit",
    
    [Parameter(Mandatory=$false)]
    [switch]$LoadEnv
)

# Change to backend directory
$BackendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $BackendDir

Write-Host "Running $TestType tests..." -ForegroundColor Cyan

# Load .env file if requested
if ($LoadEnv) {
    $EnvFile = Join-Path (Split-Path -Parent $BackendDir) ".env"
    
    if (Test-Path $EnvFile) {
        Write-Host "[LOAD] Loading environment variables from .env file..." -ForegroundColor Yellow
        
        Get-Content $EnvFile | ForEach-Object {
            if ($_ -match '^([^=#]+)=(.*)$') {
                $name = $matches[1].Trim()
                $value = $matches[2].Trim()
                
                # Remove quotes if present
                $value = $value -replace '^["`''](.*)["'']$', '$1'
                
                Set-Item -Path "env:$name" -Value $value
                Write-Host "  [OK] Loaded: $name" -ForegroundColor Gray
            }
        }
        Write-Host ""
    } else {
        Write-Host "[WARNING] .env file not found at: $EnvFile" -ForegroundColor Yellow
        Write-Host "          Integration tests will skip if Azure resources are not configured" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Determine Maven test arguments based on test type
$mavenArgs = switch ($TestType) {
    "unit" { "mvn test" }
    "integration" { "mvn test -Dgroups=integration" }
    "all" { "mvn test -Dgroups=" }
}

# Build Docker command with environment variables if loaded
$dockerCmd = "docker run --rm -v `"${PWD}:/app`" -w /app"

if ($LoadEnv) {
    # Add Azure environment variables if they exist
    $azureEnvVars = @(
        "AZURE_SERVICEBUS_CONNECTION_STRING",
        "AZURE_EVENTGRID_ENDPOINT",
        "AZURE_EVENTGRID_KEY",
        "AZURE_EVENTGRID_TOPIC_ENDPOINT",
        "AZURE_SERVICE_BUS_CONNECTION_STRING",
        "AZURE_EVENT_GRID_DOMAIN_ENDPOINT",
        "AZURE_EVENT_GRID_DOMAIN_KEY",
        "AZURE_EVENT_GRID_TOPIC_ENDPOINT",
        "AZURE_EVENT_GRID_TOPIC_KEY"
    )
    
    foreach ($envVar in $azureEnvVars) {
        $value = [Environment]::GetEnvironmentVariable($envVar)
        if ($value) {
            $dockerCmd += " -e $envVar=`"$value`""
        }
    }
}

$dockerCmd += " maven:3.9-eclipse-temurin-17 $mavenArgs"

# Run tests in Docker
Write-Host "Executing: $dockerCmd" -ForegroundColor Gray
Write-Host ""

$startTime = Get-Date
Invoke-Expression $dockerCmd

$exitCode = $LASTEXITCODE
$duration = (Get-Date) - $startTime

Write-Host ""
Write-Host "================================================================" -ForegroundColor Gray

if ($exitCode -eq 0) {
    Write-Host "[SUCCESS] All tests passed!" -ForegroundColor Green
    Write-Host "[TIME] Duration: $($duration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Gray
} else {
    Write-Host "[FAILED] Tests failed with exit code: $exitCode" -ForegroundColor Red
    Write-Host "[TIME] Duration: $($duration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Gray
}

Write-Host "================================================================" -ForegroundColor Gray
Write-Host ""

exit $exitCode
