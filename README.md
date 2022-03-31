# wallet-stat-api
```
make migrate
```
```
make commit-image
```
```
./gradlew generateXmlJooq --rerun-tasks -P dbUrl=jdbc:postgresql://localhost:5432/wallet -P dbUser=postgres -P dbPassword=postgres
./gradlew generateJavaJooq --rerun-tasks
```

save transaction record:
```
curl --request POST \
  --url 'http://localhost:8080/wallet/top_up?=' \
  --header 'Content-Type: application/json' \
  --data '{
	"datetime": "2022-03-31T01:01:00+03:00",
	"amount": 1.1
}'
```

Get balance history
```
curl http://localhost:8080/wallet/history?start_datetime=2011-10-05T10%3A48%3A01%2B00%3A00&end_datetime=2022-10-05T18%3A48%3A02%2B00%3A00
```