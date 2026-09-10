# Hexaq
# 계층: Scripts
# 객체: cloudflare-tunnel.ps1
# 책임: 로컬 8282 를 Quick Tunnel(https://*.trycloudflare.com)로 연다. 프로세스를 끄면 URL 이 죽는다.
# 문서: [docs/technical/03-cloudflare-tunnel.md](../docs/technical/03-cloudflare-tunnel.md)
#
# Requires cloudflared. Publishes local Spring Boot (8282) at a trycloudflare.com URL.
# Keep this process running. The URL changes each start (Quick Tunnel).
$ErrorActionPreference = "Stop"
$cloudflared = Get-Command cloudflared -ErrorAction SilentlyContinue
if (-not $cloudflared) {
	$candidates = @(
		"${env:ProgramFiles}\cloudflared\cloudflared.exe",
		"${env:ProgramFiles(x86)}\cloudflared\cloudflared.exe"
	)
	$found = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
	if (-not $found) {
		Write-Error "cloudflared가 없습니다. winget install --id Cloudflare.cloudflared -e"
	}
	$exe = $found
} else {
	$exe = $cloudflared.Source
}

Write-Host "Tunnel -> http://127.0.0.1:8282"
& $exe tunnel --url http://127.0.0.1:8282 --no-autoupdate
