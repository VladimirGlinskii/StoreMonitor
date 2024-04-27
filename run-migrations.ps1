mvn -f ./liquibase/pom.xml `
  -DdbUsername="vglinskii" `
  -DdbPassword="adminadmin" `
  -DdbUrl="jdbc:mysql://rc1a-v47b0x3acmofrfcp.mdb.yandexcloud.net:3306/base-api" `
  liquibase:update
