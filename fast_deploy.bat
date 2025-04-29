@echo off
set APP_NAME=roomily
set VERSION=1.0.0
set NETWORK=pi_shared_net
set DEPLOY_CONNECT=pi@api.roomily.tech

docker build --platform linux/arm64 -t %APP_NAME%:%VERSION% -f FastDockerfile .
docker save -o %APP_NAME%.tar %APP_NAME%

scp -o StrictHostKeyChecking=no .\%APP_NAME%.tar %DEPLOY_CONNECT%:~
scp -o StrictHostKeyChecking=no ./docker-compose.yml %DEPLOY_CONNECT%:~
ssh -o StrictHostKeyChecking=no %DEPLOY_CONNECT% ^
  "docker load -i %APP_NAME%.tar && docker rm -f %APP_NAME% || true && docker run -d --name %APP_NAME% -p 8080:8080 --network %NETWORK% -e TZ=Asia/Ho_Chi_Minh -e VIRTUAL_HOST=\"api.roomily.tech\" -e VIRTUAL_PORT=8080 -e LETSENCRYPT_HOST=\"api.roomily.tech\" -e LETSENCRYPT_EMAIL=\"blog@api.roomily.tech\" %APP_NAME%:%VERSION%"