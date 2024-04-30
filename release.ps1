.\build-base-api.ps1
if ($LASTEXITCODE -ne 0) {
  throw "Failed to build base api image"
  return
}

.\build-functions.ps1
if ($LASTEXITCODE -ne 0) {
  throw "Failed to build cloud functions"
  return
}

terraform -chdir=ops-tools apply -auto-approve
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply terraform config"
  return
}

.\run-migrations.ps1
