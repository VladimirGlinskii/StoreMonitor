$ErrorActionPreference = "Stop"

mvn -f ./liquibase/pom.xml `
  -DdbUsername="root" `
  -DdbPassword="root" `
  -DdbUrl="jdbc:mysql://localhost:3306/store-monitor-test" `
  liquibase:update

$functionNames = 'auth-function', 'cashier-simulator', 'update-sensor-value', 'sensor-simulator', 'create-incident', `
  'incidents-report-function', 'decommissioned-report-simulator'

mvn -f ".\base-api\pom.xml" -DskipUTs integration-test

foreach ($functionName in $functionNames) {
  mvn -f ".\functions-parent\$functionName\pom.xml" -DskipUTs integration-test
}
