locals {
  dbUrl         = "jdbc:mysql://c-${yandex_mdb_mysql_cluster.db-cluster.id}.rw.mdb.yandexcloud.net:3306/base-api"
  baseApiUrl    = "https://${yandex_api_gateway.pa-api-gateway.domain}/api"
  devicesApiUrl = "https://${yandex_api_gateway.devices-api-gateway.domain}/api"
}
