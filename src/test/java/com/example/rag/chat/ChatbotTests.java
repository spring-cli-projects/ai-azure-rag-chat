package com.example.rag.chat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
