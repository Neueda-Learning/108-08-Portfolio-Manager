#!/usr/bin/env bash
# One-shot bootstrap for a fresh machine that already has the Docker engine
# installed but not the Docker Compose plugin. Installs Compose if missing,
# then builds and starts the stack. No .env or manual config required.
set -euo pipefail

ARCH=$(uname -m)
case "$ARCH" in
  x86_64) ARCH="amd64" ;;
  aarch64|arm64) ARCH="arm64" ;;
esac
COMPOSE_ARCH=$(uname -m)

CLI_PLUGINS_DIR="/usr/local/lib/docker/cli-plugins"
sudo mkdir -p "$CLI_PLUGINS_DIR"
mkdir -p "$HOME/.docker/cli-plugins"

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin not found — installing..."

  # Install the official Compose plugin binary directly. This works on any
  # distro (Amazon Linux, Ubuntu, etc.) without depending on a package
  # manager having docker-compose-plugin available.
  sudo curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${COMPOSE_ARCH}" \
    -o "$CLI_PLUGINS_DIR/docker-compose"
  sudo chmod +x "$CLI_PLUGINS_DIR/docker-compose"

  # Also drop a copy under the invoking user's own cli-plugins dir in case
  # Docker isn't configured to look at the system-wide plugin path.
  cp "$CLI_PLUGINS_DIR/docker-compose" "$HOME/.docker/cli-plugins/docker-compose"
fi

echo "Docker Compose ready: $(docker compose version)"

# `docker compose build` needs buildx >= 0.17.0. Install/upgrade it if the
# installed version is missing or older than that.
REQUIRED_BUILDX="0.17.0"
CURRENT_BUILDX=$(docker buildx version 2>/dev/null | grep -oE 'v[0-9]+\.[0-9]+\.[0-9]+' | head -1 | tr -d v || true)

if [ -z "$CURRENT_BUILDX" ] || [ "$(printf '%s\n%s\n' "$REQUIRED_BUILDX" "$CURRENT_BUILDX" | sort -V | head -1)" != "$REQUIRED_BUILDX" ]; then
  echo "Installing/upgrading buildx (found: ${CURRENT_BUILDX:-none}, need >= $REQUIRED_BUILDX)..."
  BUILDX_TAG=$(curl -sSL https://api.github.com/repos/docker/buildx/releases/latest | grep -oE '"tag_name": *"[^"]+"' | cut -d'"' -f4)
  sudo curl -SL "https://github.com/docker/buildx/releases/download/${BUILDX_TAG}/buildx-${BUILDX_TAG}.linux-${ARCH}" \
    -o "$CLI_PLUGINS_DIR/docker-buildx"
  sudo chmod +x "$CLI_PLUGINS_DIR/docker-buildx"
  cp "$CLI_PLUGINS_DIR/docker-buildx" "$HOME/.docker/cli-plugins/docker-buildx"
fi

echo "Docker Buildx ready: $(docker buildx version)"

cd "$(dirname "$0")"
docker compose up -d --build

echo ""
echo "Stack starting. Once containers are healthy:"
echo "  Frontend: http://$(hostname -I | awk '{print $1}'):8082"
echo "  Backend:  http://$(hostname -I | awk '{print $1}'):8083"
echo "  MySQL:    port 8084"
