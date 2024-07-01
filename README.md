# Spring AI Retrieval Augmented Generation with Azure OpenAI

## Introduction

Retrieval Augmented Generation (RAG) is a technique that integrates your data into the AI model's responses.
This project demonstrates Retrieval Augmented Generation in practice and can serve as the foundation for customizing to meet your specific requirements in your own project.

## How it works

First, you need to upload the documents you wish to have analyzed in an AI response into a Vector Database.
This involves breaking down the documents into smaller segments because AI models typically only manage to process a few tens of kilobytes of custom data for generating responses.
After splitting, these document segments are stored in the Vector Database.

This first step was done using the code in the repository https://github.com/spring-cli-projects/ai-azure-rag-load

The second step involves including data from the Vector Database that is pertinent to your query when you make a request to the AI model.
This is achieved by performing a similarity search within the Vector Database to identify relevant content and merging it with your original user request text.  This steps is done using Spring AI's `QuestionAnswerAdvisor` in the `ChatClient` fluent api.

## Endpoints

This project contains a web service with the following endpoints under http://localhost:8080

* GET `/rag/chatbot`

The `/rag/chatbot` endpoint takes a `question` parameter which is the question you want to ask the AI model.

## Prerequisites

### Loading the Data into the Azure AI Search Vector Store

You should have already run the application in the repository https://github.com/spring-cli-projects/ai-azure-rag-load


### Azure OpenAI setup

1. Obtain your Azure OpenAI `endpoint` and `api-key` from the Azure OpenAI Service section on [Azure Portal](https://portal.azure.com) and deploy a chat model, such as `gpt-35-turbo-16k` and an embedding model such as `text-embedding-ada-002`.

2. Create an instance of [Azure AI Search vector database](https://azure.microsoft.com/en-us/products/ai-services/ai-search/) and obtain the API keys and URL.

Here is the `application.yml` file for the application. You will need to fill in the appropriate API keys, model names, and endpoints.
Only the API keys have been removed in the configuration.
```yaml
spring:
  ai:
    azure:
      openai:
        api-key:
        endpoint: https://springai.openai.azure.com/
        chat:
          options:
            deployment-name: gpt-4o
        embedding:
          options:
            deployment-name: text-embedding-ada-002
    vectorstore:
      azure:
        api-key:
        url: https://springaisearch.search.windows.net
        index-name: carina_index
```


## Running the application

```
./mvnw spring-boot:run
```

Will load the data and exit the application

## Chat with the document

Send your question to the Carina ChatBot using

```shell
http --body --unsorted localhost:8080/rag/chatbot question=="What is the purpose of Carina?"

```

The response is

```json
{
    "question": "What is the purpose of Carina?",
    "answer": "The purpose of Carina is to provide a safe and easy-to-use online platform for individuals and families to find home care or child care services. It also helps care professionals, known as Individual Providers (IPs), to connect with individuals and families in need of care. Carina aims to strengthen communities by prioritizing people and supporting care workers."
}

```

## Change the version of the document being used

The application is defaults to answer questions using version 2 of the PDF.  If we ask the chatbot, "When was Carina founded?"

```shell
http --body localhost:8080/rag/chatbot question=="When was Carina founded?"
```

The response is `Carina was founded in 2017.`

Using version 1 of the PDF by passing in the `version` request parameter gives

```shell
http --body localhost:8080/rag/chatbot question=="When was Carina founded?" version==1
```

The response is `Carina was founded in 2016.`


## Evaluation Driven Development

There is a small JUnit test that uses the `RelevancyEvaluator` to show how you can test your AI application. 
The code is as following

```java
@SpringBootTest
public class ChatbotTests {
    
    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void testEvaluation() {

        var question = "What is the purpose of Carina?";

        ChatClient chatClient = builder.build();

        ChatResponse chatResponse = chatClient
                .prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.query("version == 2")))
                .user("What is the purpose of Carina?")
                .call()
                .chatResponse();

        var relevancyEvaluator = new RelevancyEvaluator(this.builder);

        EvaluationRequest evaluationRequest = new EvaluationRequest(question, List.of(), chatResponse);
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertTrue(evaluationResponse.isPass(), "Response is not relevant to the question");

    }
}
```

The `RelevancyEvalutor` will determine, using the AI Model itself, if the answer for the question is relevant.

