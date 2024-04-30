$ErrorActionPreference = "Stop"

$registryId = 'crp24s5n75vk52ot582f'
$repositoryName = 'store-monitor-base-api'
$tag = 'development'

docker build . -t cr.yandex/$registryId/${repositoryName}:$tag -f ./ops-tools/Dockerfile
if ($LASTEXITCODE -ne 0) {
  throw "Failed to build base api image"
}

docker push cr.yandex/$registryId/${repositoryName}:$tag
if ($LASTEXITCODE -ne 0) {
  throw "Failed to push base api image to registry"
}

$Env:TF_VAR_base_api_image_digest = $(docker inspect cr.yandex/$registryId/${repositoryName}:$tag --format '{{.Id}}')
