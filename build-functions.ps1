mvn -f ./common/pom.xml install
mvn -f ./functions-parent/pom.xml install

$functionNames = 'auth-function', 'cashier-simulator', 'update-sensor-value', 'sensor-simulator', 'create-incident', `
  'incidents-report-function'
foreach ($functionName in $functionNames) {
  $targetZipName = "functions-build\$functionName.zip"
  if (Test-Path $targetZipName) {
    Remove-Item $targetZipName
  }
  7z u $targetZipName ".\functions-parent\$functionName\target\code\*"
}
