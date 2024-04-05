$registryId = 'crp24s5n75vk52ot582f'
docker build . -t cr.yandex/$registryId/store-monitor-base-api:development -f ./ops-tools/Dockerfile
docker push cr.yandex/$registryId/store-monitor-base-api:development
