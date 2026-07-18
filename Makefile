.PHONY: test run docker-up docker-down verify

test:
	mvn test

verify:
	mvn verify

run:
	mvn spring-boot:run

docker-up:
	docker compose up --build

docker-down:
	docker compose down -v
