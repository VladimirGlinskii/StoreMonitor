$ErrorActionPreference = "Stop"

mvn -f ./common/pom.xml install
if ($LASTEXITCODE -ne 0) {
  throw "Failed to build common module"
}

mvn -f ./functions-parent/pom.xml -DskipTests install
if ($LASTEXITCODE -ne 0) {
  throw "Failed to build cloud functions"
}

$functionNames = 'auth-function', 'cashier-simulator', 'update-sensor-value', 'sensor-simulator', 'create-incident', `
  'incidents-report-function', 'decommissioned-report-simulator'
foreach ($functionName in $functionNames) {
  $targetZipName = "functions-build\$functionName.zip"
  if (Test-Path $targetZipName) {
    Remove-Item $targetZipName
  }
  7z u $targetZipName ".\functions-parent\$functionName\target\code\*"
}
