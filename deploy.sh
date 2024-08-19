image_version=$1
docker_registry=$2
container_name=$3

discord_token=$4
guild_id=$5
roles_message_channel_id=$6
roles_message_id=$7
daily_updates_channel_id=$8

if [ $# -lt 8 ] || [ $# -gt 8 ]; then
        echo Usage: \'$0 \<image_version\> \<container_registry\> \<container_name\> \<discord_token\> \<guild_id\> \<roles_message_channel_id\> \<roles_message_id\> \<daily_updates_channel_id\>\'
        exit 1
fi

image_name=$docker_registry/kbot:$image_version

docker stop $container_name || true
docker rm $container_name || true
docker pull $image_name
docker run -d --restart=always --name $container_name \
    -e DISCORD_TOKEN=$discord_token \
    -e GUILD_ID=$guild_id \
    -e ROLES_MESSAGE_CHANNEL_ID=$roles_message_channel_id \
    -e ROLES_MESSAGE_ID=$roles_message_id \
    -e DAILY_UPDATES_CHANNEL_ID=$daily_updates_channel_id \
    $image_name