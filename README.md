# Store Monitor

## Initialization

1. First of all, you need to set next terraform configuration
   variables in the `variables.tf` file:
   1. cloud_id - ID of the cloud where project need to be created;
   2. db_user - Username for the main database user;
   3. db_password - Password for the main database user.
2. Add yandex authentication data to env variables using the
   `terraform-auth.ps1` helper script.
3. Run the `build-functions.ps1` script.
3. Run `terraform -chdir=ops-tools apply -input=false`.
4. Fill `repositoryId` variable in the `build-base-api.ps1` file with 
   id of the created container registry.
5. Fill `dbUsername`, `dbPassword` and `dbUrl` parameters in the
   `run-migrations.ps1` file with appropriate values.
6. Run the `release.ps1` script.

## Deploy

If you need to update your software:
1. Add yandex authentication data to env variables using the
   `terraform-auth.ps1` helper script.
2. Run the `release.ps1` script.

## Testing

1. Run docker container with testing database with username `root`, 
   password `root` and name `store-monitor-test`.

   For example, you can use the following command:

   `docker run --name store-monitor-test 
   -e MYSQL_DATABASE=store-monitor-test -e MYSQL_ROOT_PASSWORD=root
   -d mysql:8.3.0`
2. Run the `run-tests.ps1` script.
