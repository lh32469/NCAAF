#!/usr/bin/env bash

# Consul server is Docker host
HOST=$(ip route | grep default | awk '{ print $3 }')

trap deregister 1 2 3 6 15

deregister() {
    echo "Container stopping, deregistering with Consul..."
    curl -X PUT http://${HOST}:8500/v1/agent/service/deregister/$(hostname)
    exit
}

#
# Start Java Application in background
#
java -jar ncaaf.jar server docker.yml &

# Wait for App to start
sleep 10

#
# Test App
#
curl localhost:9020/status/ping

#
# Register App with Consul
#
IP=$(hostname -I)
# Strip trailing \n
IP=${IP%%[[:space:]]}

curl -X PUT -d "{\
	\"Name\": \"${SERVICE}\", \
	\"ID\": \"$(hostname)\", \
	\"Address\": \"${IP}\" }" \
  http://${HOST}:8500/v1/agent/service/register


wait $!

deregister
