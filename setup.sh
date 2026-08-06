#!/usr/bin/env bash
# One-shot bootstrap for a fresh machine that already has the Docker engine
# installed but not the Docker Compose plugin. Installs Compose if missing,
# then builds and starts the stack. No .env or manual config required.
set -euo pipefail

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin not found — installing..."

  # Install the official Compose plugin binary directly. This works on any
  # distro (Amazon Linux, Ubuntu, etc.) without depending on a package
  # manager having docker-compose-plugin available.
  ARCH=$(uname -m)
  case "$ARCH" in
    x86_64) ARCH="x86_64" ;;
    aarch64|arm64) ARCH="aarch64" ;;
  esac

  CLI_PLUGINS_DIR="/usr/local/lib/docker/cli-plugins"
  sudo mkdir -p "$CLI_PLUGINS_DIR"
  sudo curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
    -o "$CLI_PLUGINS_DIR/docker-compose"
  sudo chmod +x "$CLI_PLUGINS_DIR/docker-compose"

  # Also drop a copy under the invoking user's own cli-plugins dir in case
  # Docker isn't configured to look at the system-wide plugin path.
  mkdir -p "$HOME/.docker/cli-plugins"
  cp "$CLI_PLUGINS_DIR/docker-compose" "$HOME/.docker/cli-plugins/docker-compose"
fi

echo "Docker Compose ready: $(docker compose version)"

cd "$(dirname "$0")"
docker compose up -d --build

echo ""
echo "Stack starting. Once containers are healthy:"
echo "  Frontend: http://$(hostname -I | awk '{print $1}'):8082"
echo "  Backend:  http://$(hostname -I | awk '{print $1}'):8083"
echo "  MySQL:    port 8084"
