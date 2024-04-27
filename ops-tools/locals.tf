locals {
  dbUrl         = "jdbc:mysql://c-${module.mysql.cluster_id}.rw.mdb.yandexcloud.net:3306/base-api"
  baseApiUrl    = "https://${yandex_api_gateway.pa_api_gateway.domain}/api"
  devicesApiUrl = "https://${yandex_api_gateway.devices_api_gateway.domain}/api"
}
