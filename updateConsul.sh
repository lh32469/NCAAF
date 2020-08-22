# Self register container hostname/ip with Consul

IP=$(hostname -I)
# Strip trailing \n
IP=${IP%%[[:space:]]}

# Consul server is Docker host
HOST=$(ip route | grep default | awk '{ print $3 }')

curl -X PUT -d "{\"Name\": \"$(hostname)\", \
	\"Address\": \"${IP}\" }" \
 http://${HOST}:8500/v1/agent/service/register

