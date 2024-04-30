$ErrorActionPreference = "Stop"

mvn -f ./liquibase/pom.xml `
  -DdbUsername="root" `
  -DdbPassword="root" `
  -DdbUrl="jdbc:mysql://localhost:3306/store-monitor-test" `
  liquibase:update
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply liquibase migration"
}

$functionNames = 'auth-function', 'cashier-simulator', 'update-sensor-value', 'sensor-simulator', 'create-incident', `
  'incidents-report-function', 'decommissioned-report-simulator'

mvn -f ".\base-api\pom.xml" integration-test

foreach ($functionName in $functionNames) {
  mvn -f ".\functions-parent\$functionName\pom.xml" integration-test
}
