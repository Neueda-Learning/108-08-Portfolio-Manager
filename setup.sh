#!/usr/bin/env bash
# One-shot bootstrap for a fresh machine that already has the Docker engine
# installed but not the Docker Compose plugin. Installs Compose if missing,
# then builds and starts the stack. No .env or manual config required.
set -euo pipefail

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin not found — installing..."

  if command -v dnf >/dev/null 2>&1; then
    sudo dnf install -y docker-compose-plugin
  elif command -v yum >/dev/null 2>&1; then
    sudo yum install -y docker-compose-plugin
  elif command -v apt-get >/dev/null 2>&1; then
    sudo apt-get update
    sudo apt-get install -y docker-compose-plugin
  else
    # Fallback: install the plugin binary directly from the Docker CLI plugins release.
    echo "No supported package manager found — installing the Compose plugin binary directly."
    ARCH=$(uname -m)
    mkdir -p "$HOME/.docker/cli-plugins"
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
      -o "$HOME/.docker/cli-plugins/docker-compose"
    chmod +x "$HOME/.docker/cli-plugins/docker-compose"
  fi
fi

echo "Docker Compose ready: $(docker compose version)"

cd "$(dirname "$0")"
docker compose up -d --build

echo ""
echo "Stack starting. Once containers are healthy:"
echo "  Frontend: http://$(hostname -I | awk '{print $1}'):8082"
echo "  Backend:  http://$(hostname -I | awk '{print $1}'):8083"
echo "  MySQL:    port 8084"
