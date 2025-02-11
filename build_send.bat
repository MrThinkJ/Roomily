@echo off
set APP_NAME=roomily
set VERSION=1.0.0
set DEPLOY_CONNECT=pi@mrthinkj.site

docker build --platform linux/arm64 -t %APP_NAME%:%VERSION% .
docker save -o %APP_NAME%.tar %APP_NAME%

scp -o StrictHostKeyChecking=no .\%APP_NAME%.tar %DEPLOY_CONNECT%:~
ssh -o StrictHostKeyChecking=no %DEPLOY_CONNECT% ^
  "docker load -i %APP_NAME%.tar && docker rm -f %APP_NAME% || true && docker run -d --name %APP_NAME% -p 8080:8080 -e VIRTUAL_HOST=\"roomily.mrthinkj.site\" -e VIRTUAL_PORT=8080 -e LETSENCRYPT_HOST=\"roomily.mrthinkj.site\" -e LETSENCRYPT_EMAIL=\"blog@roomily.mrthinkj.site\" %APP_NAME%:%VERSION%"
