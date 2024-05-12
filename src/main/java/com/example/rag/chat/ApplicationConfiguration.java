package com.example.rag.chat;

import com.azure.search.documents.indexes.SearchIndexClient;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.chatbot.ChatBot;
import org.springframework.ai.chat.chatbot.DefaultChatBot;
import org.springframework.ai.chat.prompt.transformer.QuestionContextAugmentor;
import org.springframework.ai.chat.prompt.transformer.VectorStoreRetriever;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.azure.AzureVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationConfiguration {
	@Bean
	public ChatBot chatBot(ChatClient chatClient, VectorStore vectorStore) {
		SearchRequest searchRequest = SearchRequest.defaults()
				.withFilterExpression("version == 1");

		return DefaultChatBot.builder(chatClient)
				.withRetrievers(List.of(new VectorStoreRetriever(vectorStore, searchRequest)))
				.withAugmentors(List.of(new QuestionContextAugmentor()))
				.build();
	}

	@Bean
	public AzureVectorStore vectorStore(SearchIndexClient searchIndexClient, EmbeddingClient embeddingClient) {
		var vectorStore = new AzureVectorStore(searchIndexClient, embeddingClient,
				List.of(AzureVectorStore.MetadataField.text("filename"),
						AzureVectorStore.MetadataField.int32("version")));
		vectorStore.setIndexName("carina-index");
		return vectorStore;
	}
}
