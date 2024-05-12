# Spring AI Retrieval Augmented Generation with Azure OpenAI

## Introduction

Retrieval Augmented Generation (RAG) is a technique that integrates your data into the AI model's responses.

First, you need to upload the documents you wish to have analyzed in an AI respoinse into a Vector Database.
This involves breaking down the documents into smaller segments because AI models typically only manage to process a few tens of kilobytes of custom data for generating responses.
After splitting, these document segments are stored in the Vector Database.

This first step was done using the code in the repository https://github.com/spring-cli-projects/ai-azure-rag-load

The second step involves including data from the Vector Database that is pertinent to your query when you make a request to the AI model.
This is achieved by performing a similarity search within the Vector Database to identify relevant content.

In the third step, you merge the text of your request with the documents retrieved from the Vector Database before sending it to the AI model.
This process is informally referred to as 'stuffing the prompt'.

The second and third steps are done is using the code in this repository via the help of the Spring AI `ChatBot` class.

This project demonstrates Retrieval Augmented Generation in practice and can serve as the foundation for customizing to meet your specific requirements in your own project.

## Endpoints

This project contains a web service with the following endpoints under http://localhost:8080

* GET `/rag/chatbot`

The `/rag/chatbot` endpoint takes a `question` parameter which is the question you want to ask the AI model.

## Prerequisites

### Loading the Data into the Azure AI Search Vector Store

You should have already run the application in the repository https://github.com/spring-cli-projects/ai-azure-rag-load


### Azure OpenAI Credentials

Obtain your Azure OpenAI `endpoint` and `api-key` from the Azure OpenAI Service section on [Azure Portal](https://portal.azure.com)

The Spring AI project defines a configuration property named `spring.ai.azure.openai.api-key` that you should set to the value of the `API Key` obtained from Azure

Exporting an environment variables is one way to set these configuration properties.
```shell
export SPRING_AI_AZURE_OPENAI_API_KEY=<INSERT KEY HERE>
export SPRING_AI_AZURE_OPENAI_ENDPOINT=<INSERT ENDPOINT URL HERE>
export SPRING_AI_AZURE_OPENAI_CHAT_OPTIONS_DEPLOYMENT_NAME=<INSERT NAME HERE>
```
Note, the `/resources/application.yml` references the environment variable `${SPRING_AI_AZURE_OPENAI_API_KEY}`.

## Azure AI Search VectorStore

This sample uses the Azure AI Search VectorStore. 
Information on how to set that up is in the `README.md` of the repository https://github.com/spring-cli-projects/ai-azure-rag-load


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

The application is setup to answer questions about version 1 of the PDF.  If we ask the chatbot, "When was Carina founded?"

```shell
http --body --unsorted localhost:8080/rag/chatbot question=="When was Carina founded?"

```

The response is 

```json
{
    "question": "When was Carina founded?",
    "answer": "Carina was founded in 2016."
}
```

However, if we change the definition of the `SearchRequest` used with the `ChatBot` from

```java
	@Bean
	public ChatBot chatBot(ChatClient chatClient, VectorStore vectorStore) {
		SearchRequest searchRequest = SearchRequest.defaults()
				.withFilterExpression("version == 1");

		return DefaultChatBot.builder(chatClient)
				.withRetrievers(List.of(new VectorStoreRetriever(vectorStore, searchRequest)))
				.withAugmentors(List.of(new QuestionContextAugmentor()))
				.build();
	}
```

to 

```java
	@Bean
	public ChatBot chatBot(ChatClient chatClient, VectorStore vectorStore) {
		SearchRequest searchRequest = SearchRequest.defaults()
				.withFilterExpression("version == 2");

		return DefaultChatBot.builder(chatClient)
				.withRetrievers(List.of(new VectorStoreRetriever(vectorStore, searchRequest)))
				.withAugmentors(List.of(new QuestionContextAugmentor()))
				.build();
	}
```

and ask the same question, the response is 

```json
{
    "question": "When was Carina founded?",
    "answer": "Carina was founded in 2017."
}
```

Since the second version of the PDF document has updated the date to be 2017.


## Evaluation Driven Development

There is a small JUnit test that uses the `RelevancyEvaluator` to show how you can test your AI application.
The code is as following


```java

@SpringBootTest
public class ChatbotTests {


    @Autowired
    private ChatBot chatBot;

    @Autowired
    private ChatClient chatClient;

    @Test
    void testEvaluation() {

        var prompt = new Prompt(new UserMessage("What is the purpose of Carina?"));
        ChatBotResponse chatBotResponse = chatBot.call(new PromptContext(prompt));

        var relevancyEvaluator = new RelevancyEvaluator(this.chatClient);

        EvaluationRequest evaluationRequest = new EvaluationRequest(chatBotResponse);
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertTrue(evaluationResponse.isPass(), "Response is not relevant to the question");
    }
}

```


