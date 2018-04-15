build:
	./run-in-docker.sh mvn package -DskipTests

gen-ts:
	./run-in-docker.sh generate -i k8s-schema/swagger-1.9.6.json -l typescript-k8s -o out/ts

