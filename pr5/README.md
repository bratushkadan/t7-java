# Prac 5

## Run application

### Locally

```bash
APP_PORT=8000 mvn spring-boot:run
```

or 

```bash
mvn package && java -jar ./target/app.jar
```

### Docker

build:
```bash
docker build . -t rksp-pr5-service:0.0.1
```

run:
```bash
docker run \
    --name rksp-pr5-service \
    -e APP_PORT=8080 \
    -p 8080:8080 \
    --network host \
    -d \
    rksp-pr5-service:0.0.1
```

## Spin Up Database

```bash
docker run \
    --name rksp_pr5_postgres \
    -e POSTGRES_USER=root \
    -e POSTGRES_PASSWORD=root \
    -e POSTGRES_DB=db \
    -p 5432:5432 \
    --network host \
    -d \
    postgres:16.4-bookworm
```
