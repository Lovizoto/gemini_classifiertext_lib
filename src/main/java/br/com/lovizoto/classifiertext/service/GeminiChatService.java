package br.com.lovizoto.classifiertext.service;


import br.com.lovizoto.classifiertext.model.Content;
import br.com.lovizoto.classifiertext.model.GeminiRequest;
import br.com.lovizoto.classifiertext.model.GenerationConfig;
import br.com.lovizoto.classifiertext.model.Part;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class GeminiChatService {

    private static final Logger log = LoggerFactory.getLogger(GeminiChatService.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String apiUrl;
    private String apiKey;

    public GeminiChatService(String apiUrl, String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public Optional<String> generateContent(List<Content> history, GenerationConfig config) {
        return sendRequestToGemini(history, config);
    }

    /*
        O método recebe o CONTEXTO (regras de classificação) e o TEXTO a ser classificado
     */
    public Optional<String> classifyText(String systemContext, String textToClassify) {

        //Não há uso de um histórico longo, apenas um prompt direto com contexto e o texto a classificar.
        String fullPrompt = systemContext + "\n\n--- TEXTO PARA CLASSIFICAR ---\n\"" + textToClassify + "\"";

        List<Content> contents = List.of(
                new Content("user", List.of(new Part(fullPrompt)))
        );

        // A configuração é colocada de "baixa criatividade", ou seja, temperatura baixa.
        // A resposta é mais consistente e direta na classificação
        GenerationConfig config = new GenerationConfig();
        config.setTemperature(0.1f);
        config.setMaxOutputTokens(50); // Uma resposta curta é esperada (só a categoria)


        return sendRequestToGemini(contents, config).map(String::trim);
    }


    private Optional<String> sendRequestToGemini(List<Content> contents, GenerationConfig config) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("A chave da API do Gemini não foi configurada.");
            return Optional.of("ERRO: A API Key não foi configurada.");
        }

        try {
            GeminiRequest requestPayload = new GeminiRequest();
            requestPayload.setContents(contents);
            requestPayload.setGenerationConfig(config);

            String requestBody = objectMapper.writeValueAsString(requestPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl + "?key=" + this.apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            log.debug("Enviando requisição para o Gemini: {}", requestBody);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.debug("Resposta recebida do Gemini: {}", response.body());
                return Optional.ofNullable(
                        objectMapper.readTree(response.body())
                                .path("candidates").get(0)
                                .path("content").path("parts").get(0)
                                .path("text").asText(null) // Retorna null se não encontrar
                );
            } else {
                log.error("Erro na API do Gemini ({}): {}", response.statusCode(), response.body());
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Falha ao se comunicar com a API do Gemini.", e);
            return Optional.empty();
        }
    }
}


