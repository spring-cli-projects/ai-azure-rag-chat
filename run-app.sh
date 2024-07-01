#!/bin/sh


if [ -z "$RESOURCE_GROUP" ] || [ -z "$SPRING_APPS_SERVICE" ]; then
    echo "Error: RESOURCE_GROUP and SPRING_APPS_SERVICE environment variables must be set."
    exit 1
fi

az spring create --resource-group ${RESOURCE_GROUP} --name si-mark-spring-ai-rag

az spring app create --name spring-ai-rag --resource-group ${RESOURCE_GROUP} --service si-mark-spring-ai-rag --assign-endpoint true

az spring app deploy --resource-group ${RESOURCE_GROUP} --service si-mark-spring-ai-rag --name spring-ai-rag --artifact-path /home/mark/spring-cli-projects/ai-azure-rag-chat/target/spring-ai-rag-chat-0.0.1-SNAPSHOT.jar --runtime-version Java_17


https://si-mark-spring-ai-rag-spring-ai-rag.azuremicroservices.io