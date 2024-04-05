mvn -f ./functions-parent/pom.xml install

$functionNames = 'auth-function', 'cashier-simulator'
foreach ($functionName in $functionNames) {
  Compress-Archive -Path ".\functions-parent\$functionName\target\code\*" `
    -CompressionLevel Fastest `
    -DestinationPath ".\functions-build\$functionName.zip"
}
