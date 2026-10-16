# Computer Rental System

Spring Boot and Thymeleaf application using Java 17.

## Run locally

Run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`) and open http://localhost:8080.

## Deploy on Render

1. Push the Dockerfile, .dockerignore, render.yaml, and application.properties to GitHub.
2. Sign into https://dashboard.render.com with your GitHub account.
3. Select **New > Blueprint** and connect this repository.
4. Review the free web service described in render.yaml and deploy it.
5. When deployment succeeds, open the HTTPS URL displayed by Render.

The deployment files belong in the repository root, beside pom.xml. If the repository has an extra enclosing directory, set the Blueprint path to that directory's render.yaml and set the service Root Directory to the directory containing pom.xml.

Render builds the Java application in Docker and sets PORT for the running server. Future pushes to the connected branch can trigger deployments automatically.

## Data behavior

Rental data is stored in application memory and resets on restart or redeployment. All visitors share the same rental state, and the app has no login or authorization. This deployment is suitable for a public demo; use a database and access controls before using it for real customer records.
