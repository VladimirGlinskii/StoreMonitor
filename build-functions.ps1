mvn -f ./common/pom.xml install
mvn -f ./functions-parent/pom.xml install

$functionNames = 'auth-function', 'cashier-simulator', 'update-sensor-value', 'sensor-simulator'
foreach ($functionName in $functionNames) {
  tar.exe -a -c -f "functions-build\$functionName.zip" -C "functions-parent\$functionName\target\code" *
}
