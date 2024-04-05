mvn -f ./common/pom.xml install
mvn -f ./functions-parent/pom.xml install

$functionNames = 'auth-function', 'cashier-simulator'
foreach ($functionName in $functionNames) {
  Compress-Archive -Path ".\functions-parent\$functionName\target\code\*" `
    -CompressionLevel Fastest `
    -Update `
    -DestinationPath ".\functions-build\$functionName.zip"
}
