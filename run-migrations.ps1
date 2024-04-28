mvn -f ./liquibase/pom.xml `
  -DdbUsername="vglinskii" `
  -DdbPassword="adminadmin" `
  -DdbUrl="jdbc:mysql://rc1a-rt3g2wz6syvcaq0c.mdb.yandexcloud.net:3306/base-api" `
  liquibase:update
