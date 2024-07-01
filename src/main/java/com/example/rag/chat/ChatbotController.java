package com.example.rag.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rag/chatbot")
public class ChatbotController {

	private final ChatClient chatClient;

	private final VectorStore vectorStore;

	public ChatbotController(VectorStore vectorStore, ChatClient.Builder builder) {
		this.vectorStore = vectorStore;
		this.chatClient = builder.build();
	}

	@GetMapping
	public String query(
			@RequestParam(value = "question", defaultValue = "What is the purpose of Carina?") String question,
			@RequestParam(value = "version", defaultValue = "2") String version) {

		var filterExpression = "version == " + version; // portable across all vector stores
		System.out.println(filterExpression);
		return this.chatClient
				.prompt()
				.advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults().withFilterExpression(filterExpression)))
				.user(question)
				.call()
				.content();
	}


}
