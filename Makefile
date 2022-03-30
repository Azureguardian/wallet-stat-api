prepare: build clean start

start:
	docker-compose -p db run --rm waiter db:5432 -- echo "db is up"

clean:
	docker-compose -p db stop
	docker-compose -p db rm -f

build:
	docker-compose -p db build

migrate: prepare
	docker-compose -p db run psql-cli bash -c "psql -U postgres -h db -c \"CREATE DATABASE wallet\""
	docker-compose -p db run flyway -url=jdbc:postgresql://db:5432/wallet -user=postgres -password=postgres -locations=filesystem:/migrations/ -schemas=public migrate

commit-image:
	docker-compose -p db stop
	docker commit db_db_1 test-web-api


