#!/usr/bin/env bash
set -euo pipefail

DEMO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DOCKER_DIR="${DEMO_DIR}/docs/docker"
ENV_FILE="${DOCKER_DIR}/.env"
COMPOSE_FILE="${DOCKER_DIR}/compose.yml"
RUN_DIR="${DEMO_DIR}/.run"
PID_DIR="${RUN_DIR}/pids"
LOG_DIR="${RUN_DIR}/logs"
AGENT_DIR="${RUN_DIR}/skywalking-agent"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

SKYWALKING_AGENT_VERSION="${SKYWALKING_AGENT_VERSION:-9.3.0}"
SKYWALKING_AGENT_TGZ="apache-skywalking-java-agent-${SKYWALKING_AGENT_VERSION}.tgz"
SKYWALKING_AGENT_URL="${SKYWALKING_AGENT_URL:-https://archive.apache.org/dist/skywalking/java-agent/${SKYWALKING_AGENT_VERSION}/${SKYWALKING_AGENT_TGZ}}"
SKYWALKING_OAP_GRPC_PORT="${SKYWALKING_OAP_GRPC_PORT:-11800}"
SKYWALKING_UI_PORT="${SKYWALKING_UI_PORT:-18080}"
GATEWAY_PORT="${GATEWAY_PORT:-8888}"
ACCOUNT_SERVICE_PORT="${ACCOUNT_SERVICE_PORT:-8081}"
ORDER_SERVICE_PORT="${ORDER_SERVICE_PORT:-8082}"
STORAGE_SERVICE_PORT="${STORAGE_SERVICE_PORT:-8083}"
SKYWALKING_OAP_ADDR="${SKYWALKING_OAP_ADDR:-127.0.0.1:${SKYWALKING_OAP_GRPC_PORT}}"
SKYWALKING_UI_URL="${SKYWALKING_UI_URL:-http://127.0.0.1:${SKYWALKING_UI_PORT}}"

AGENT_JAR="${AGENT_DIR}/apache-skywalking-java-agent-${SKYWALKING_AGENT_VERSION}/skywalking-agent/skywalking-agent.jar"

APPS=(
  "alibaba-gateway|${DEMO_DIR}/alibaba-gateway/target/alibaba-gateway-1.0-SNAPSHOT.jar|${GATEWAY_PORT}|http://127.0.0.1:${GATEWAY_PORT}/actuator/health"
  "account-service|${DEMO_DIR}/cloud-service/account-service/target/account-service-1.0-SNAPSHOT.jar|${ACCOUNT_SERVICE_PORT}|http://127.0.0.1:${ACCOUNT_SERVICE_PORT}/api/account/health"
  "storage-service|${DEMO_DIR}/cloud-service/storage-service/target/storage-service-1.0-SNAPSHOT.jar|${STORAGE_SERVICE_PORT}|http://127.0.0.1:${STORAGE_SERVICE_PORT}/api/storage/health"
  "order-service|${DEMO_DIR}/cloud-service/order-service/target/order-service-1.0-SNAPSHOT.jar|${ORDER_SERVICE_PORT}|http://127.0.0.1:${ORDER_SERVICE_PORT}/api/order/health"
)

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "缺少命令: $1"
    exit 1
  fi
}

ensure_prerequisites() {
  need_cmd docker
  need_cmd mvn
  need_cmd java
  need_cmd curl
  need_cmd tar
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo "缺少环境变量文件: ${ENV_FILE}"
    exit 1
  fi
  if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "缺少编排文件: ${COMPOSE_FILE}"
    exit 1
  fi
  mkdir -p "${PID_DIR}" "${LOG_DIR}" "${AGENT_DIR}"
}

ensure_agent() {
  if [[ -f "${AGENT_JAR}" ]]; then
    echo "SkyWalking Agent 已存在: ${AGENT_JAR}"
    return
  fi

  local tgz_path="${AGENT_DIR}/${SKYWALKING_AGENT_TGZ}"
  echo "下载 SkyWalking Agent: ${SKYWALKING_AGENT_URL}"
  curl -fL "${SKYWALKING_AGENT_URL}" -o "${tgz_path}"
  tar -xzf "${tgz_path}" -C "${AGENT_DIR}"
  rm -f "${tgz_path}"

  if [[ ! -f "${AGENT_JAR}" ]]; then
    echo "SkyWalking Agent 下载或解压失败: ${AGENT_JAR}"
    exit 1
  fi
}

start_infra() {
  echo "启动统一基础设施编排 (MySQL/Nacos/Seata/SkyWalking)..."
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d
}

build_apps() {
  echo "打包服务..."
  mvn -f "${DEMO_DIR}/pom.xml" \
    -pl alibaba-gateway,cloud-service/account-service,cloud-service/storage-service,cloud-service/order-service \
    -am clean package -DskipTests
}

wait_http() {
  local name="$1"
  local url="$2"
  local retries="${3:-60}"
  local i
  for ((i = 1; i <= retries; i++)); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      echo "${name} 就绪: ${url}"
      return
    fi
    sleep 2
  done
  echo "${name} 未在预期时间内就绪: ${url}"
  exit 1
}

start_one_app() {
  local name="$1"
  local jar="$2"

  if [[ ! -f "${jar}" ]]; then
    echo "未找到 JAR: ${jar}"
    exit 1
  fi

  local pid_file="${PID_DIR}/${name}.pid"
  if [[ -f "${pid_file}" ]]; then
    local pid
    pid="$(cat "${pid_file}")"
    if kill -0 "${pid}" >/dev/null 2>&1; then
      echo "${name} 已运行 (pid=${pid})"
      return
    fi
    rm -f "${pid_file}"
  fi

  local log_file="${LOG_DIR}/${name}.log"
  echo "启动 ${name} ..."
  nohup java \
    -javaagent:"${AGENT_JAR}" \
    -Dskywalking.agent.service_name="${name}" \
    -Dskywalking.collector.backend_service="${SKYWALKING_OAP_ADDR}" \
    -jar "${jar}" >"${log_file}" 2>&1 &
  echo $! >"${pid_file}"
}

start_apps() {
  local app
  for app in "${APPS[@]}"; do
    IFS="|" read -r name jar _ _ <<<"${app}"
    start_one_app "${name}" "${jar}"
  done

  for app in "${APPS[@]}"; do
    IFS="|" read -r name _ _ health_url <<<"${app}"
    wait_http "${name}" "${health_url}" 90
  done
}

verify_chain() {
  local payload
  payload='{"userId":"u1001","commodityCode":"C1001","count":1,"money":50.00}'
  echo "触发真实事务链路..."
  curl -fsS -X POST "http://127.0.0.1:${GATEWAY_PORT}/order/api/order/create" \
    -H "Content-Type: application/json" \
    -d "${payload}"
  echo
  echo "触发完成，稍后在 SkyWalking 查看链路:"
  echo "  ${SKYWALKING_UI_URL}"
}

stop_apps() {
  local app
  for app in "${APPS[@]}"; do
    IFS="|" read -r name _ _ _ <<<"${app}"
    local pid_file="${PID_DIR}/${name}.pid"
    if [[ -f "${pid_file}" ]]; then
      local pid
      pid="$(cat "${pid_file}")"
      if kill -0 "${pid}" >/dev/null 2>&1; then
        kill "${pid}"
        echo "已停止 ${name} (pid=${pid})"
      fi
      rm -f "${pid_file}"
    fi
  done
}

status() {
  local app
  for app in "${APPS[@]}"; do
    IFS="|" read -r name _ port _ <<<"${app}"
    local pid_file="${PID_DIR}/${name}.pid"
    if [[ -f "${pid_file}" ]] && kill -0 "$(cat "${pid_file}")" >/dev/null 2>&1; then
      echo "${name}: RUNNING, port=${port}, pid=$(cat "${pid_file}")"
    else
      echo "${name}: STOPPED, port=${port}"
    fi
  done
  echo
  echo "基础设施状态:"
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps
}

usage() {
  cat <<'EOF'
Usage:
  ./docs/scripts/skywalking-closure.sh start   # 启动基础设施+服务，并触发一次闭环请求
  ./docs/scripts/skywalking-closure.sh verify  # 仅触发一次闭环请求
  ./docs/scripts/skywalking-closure.sh stop    # 停止本脚本拉起的 Java 服务
  ./docs/scripts/skywalking-closure.sh status  # 查看 Java 服务与基础设施状态
EOF
}

main() {
  local cmd="${1:-}"
  case "${cmd}" in
  start)
    ensure_prerequisites
    ensure_agent
    start_infra
    build_apps
    start_apps
    verify_chain
    ;;
  verify)
    verify_chain
    ;;
  stop)
    stop_apps
    ;;
  status)
    ensure_prerequisites
    status
    ;;
  *)
    usage
    exit 1
    ;;
  esac
}

main "$@"
