# Hexaq
# 계층: Scripts
# 객체: migrate-oracle-to-mariadb.ps1
# 책임: Oracle HEXAQ_* 를 MariaDB 로 복사(ID 유지). 웹 앱 Domain 이 아님. uploads/ 는 디스크라 복사하지 않음.
# 문서: [docs/technical/02-technical-specification.md](../docs/technical/02-technical-specification.md)
#
# Copy HEXAQ_* rows Oracle -> MariaDB. Does not start the web app.
# Create the MariaDB schema first (`schema-mariadb.sql` or `bootRun --args="--spring.profiles.active=prod"`).
# Attachments in uploads/ are not copied (disk, not DB).
#
# Usage:
#   .\scripts\migrate-oracle-to-mariadb.ps1
#   .\scripts\migrate-oracle-to-mariadb.ps1 -Force
#
# Env (or parameters). Do not commit passwords.
#   ORACLE_URL / ORACLE_USERNAME / ORACLE_PASSWORD
#   MARIADB_URL / MARIADB_USERNAME / MARIADB_PASSWORD
param(
	[string]$OracleUrl = $env:ORACLE_URL,
	[string]$OracleUsername = $env:ORACLE_USERNAME,
	[string]$OraclePassword = $env:ORACLE_PASSWORD,
	[string]$MariaDbUrl = $env:MARIADB_URL,
	[string]$MariaDbUsername = $env:MARIADB_USERNAME,
	[string]$MariaDbPassword = $env:MARIADB_PASSWORD,
	[switch]$Force
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not $OracleUrl) { $OracleUrl = "jdbc:oracle:thin:@localhost:1523/XEPDB1" }
if (-not $MariaDbUrl) { $MariaDbUrl = "jdbc:mariadb://localhost:3306/hexaq" }

function Require([string]$name, [string]$value) {
	if ([string]::IsNullOrWhiteSpace($value)) {
		Write-Error "Missing $name"
	}
}

Require "ORACLE_USERNAME / -OracleUsername" $OracleUsername
Require "MARIADB_USERNAME / -MariaDbUsername" $MariaDbUsername

$env:ORACLE_URL = $OracleUrl
$env:ORACLE_USERNAME = $OracleUsername
$env:ORACLE_PASSWORD = $OraclePassword
$env:MARIADB_URL = $MariaDbUrl
$env:MARIADB_USERNAME = $MariaDbUsername
$env:MARIADB_PASSWORD = $MariaDbPassword
if ($Force) {
	$env:MIGRATE_FORCE = "true"
}

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
	$env:JAVA_HOME = "C:\01Developkit\jdk-21"
}

Write-Host "Oracle  -> $OracleUrl"
Write-Host "MariaDB -> $MariaDbUrl"
& .\gradlew.bat migrateOracleToMariaDb --no-daemon
if ($LASTEXITCODE -ne 0) {
	exit $LASTEXITCODE
}
