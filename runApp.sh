
# Consul server is Docker host
HOST=$(ip route | grep default | awk '{ print $3 }')

deregister() {
    echo "Container stopped, performing cleanup..."
    curl -X PUT http://${HOST}:8500/v1/agent/service/deregister/$(hostname)
}

trap 'deregister' SIGTERM

IP=$(hostname -I)
# Strip trailing \n
IP=${IP%%[[:space:]]}

curl -X PUT -d "{\
	\"Name\": \"${SERVICE}\", \
	\"ID\": \"$(hostname)\", \
	\"Address\": \"${IP}\" }" \
  http://${HOST}:8500/v1/agent/service/register

java -jar ncaaf.jar server docker.yml &

wait $!

deregister
