$ErrorActionPreference = "Stop"

mvn -f ./liquibase/pom.xml `
  -DdbUsername="root" `
  -DdbPassword="root" `
  -DdbUrl="jdbc:mysql://localhost:3306/store-monitor-test" `
  liquibase:update
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply liquibase migration"
}

mvn integration-test
