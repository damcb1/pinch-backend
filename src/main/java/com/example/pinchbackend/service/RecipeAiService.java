package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.response.IngredientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.example.pinchbackend.dto.response.ImportPreviewResponse;
import com.example.pinchbackend.exception.RecipeImportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service

public class RecipeAiService {
    private static final Logger log = LoggerFactory.getLogger(RecipeAiService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String  model;

    public RecipeAiService(
            ObjectMapper objectMapper,
            @Value("${anthropic.api.key}") String apiKey,
            @Value("${anthropic.api.model}") String model,
            @Value("${anthropic.api.url}") String apiUrl
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    private String callClaude(String systemPrompt, String userMessage) {
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 2048,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        JsonNode response = restClient.post()
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.path("content").path(0).path("text").asString();
    }

    public List<IngredientResponse> structureIngredients(List<String> rawLines) {
        if (rawLines == null || rawLines.isEmpty())
            return new ArrayList<>();

        String system = """
                Eres un asistente que estructura ingredientes de recetas.
                Recibes una lista de ingredientes en texto libre y devuelves SOLO un array JSON,
                sin explicaciones ni markdown. Cada elemento tiene exactamente estas claves:
                "qty" (la cantidad como texto, o null si no hay),
                "unit" (la unidad: g, ml, cda, taza, unidad..., o null),
                "name" (el ingrediente limpio, sin la cantidad ni la unidad).
                Mantén el MISMO orden y el MISMO número de elementos que la entrada.
                """;

        String user;
        try {
            user = "Estructura estos ingredientes:\n" + objectMapper.writeValueAsString(rawLines);
        } catch (Exception e) {
            user = "Estructura estos ingredientes:\n" + String.join("\n", rawLines);
        }

        String answer = callClaude(system, user);
        return parseIngredients(answer, rawLines);
    }

    private List<IngredientResponse> parseIngredients(String json, List<String> fallback) {
        try {
            JsonNode arr = objectMapper.readTree(stripFences(json));
            if (arr.isArray()) {
                List<IngredientResponse> out = new ArrayList<>();
                for (JsonNode n : arr) {
                    out.add(new IngredientResponse(
                            textOrNull(n, "qty"),
                            textOrNull(n, "unit"),
                            n.path("name").asString("")
                    ));
                }
                if (!out.isEmpty())
                    return out;
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta de la IA; uso los ingredientes sin trocear", e);
        }
        List<IngredientResponse> raw = new ArrayList<>();
        for (String line : fallback) {
            raw.add(new IngredientResponse(null, null, line));
        }
        return raw;
    }

    public ImportPreviewResponse structureFromText(String rawText) {
        String system = """
                Eres un asistente que estructura recetas a partir de texto libre
                (por ejemplo, el caption de una publicación de redes sociales).
                Devuelves SOLO un objeto JSON válido, sin explicaciones ni markdown, con estas claves:
                "title" (string),
                "timeMinutes" (entero de minutos, o null),
                "servings" (entero, o null),
                "cuisine" (tipo de cocina como texto, o null),
                "ingredients": array de objetos { "qty": string|null, "unit": string|null, "name": string },
                "steps": array de strings (cada paso por separado).
                Si un dato no aparece en el texto, ponlo a null (o array vacío).
                No inventes ingredientes ni pasos que no estén en el texto.
                """;

        String answer = callClaude(system, "Estructura esta receta:\n" + rawText);
        return parseRecipe(answer);
    }

    private ImportPreviewResponse parseRecipe(String json) {
        try {
            JsonNode r = objectMapper.readTree(stripFences(json));

            List<IngredientResponse> ingredients = new ArrayList<>();
            JsonNode ingArr = r.path("ingredients");
            if (ingArr.isArray()) {
                for (JsonNode n : ingArr) {
                    ingredients.add(new IngredientResponse(
                            textOrNull(n, "qty"),
                            textOrNull(n, "unit"),
                            n.path("name").asString("")
                    ));
                }
            }

            List<String> steps = new ArrayList<>();
            JsonNode stepArr = r.path("steps");
            if (stepArr.isArray()) {
                for (JsonNode s : stepArr) {
                    String step = s.asString("").trim();
                    if (!step.isEmpty()) steps.add(step);
                }
            }

            return new ImportPreviewResponse(
                    textOrNull(r, "title"),
                    null,
                    null,
                    intOrNull(r, "timeMinutes"),
                    intOrNull(r, "servings"),
                    textOrNull(r, "cuisine"),
                    ingredients,
                    steps
            );
        } catch (Exception e) {
            log.warn("No se pudo parsear la receta del texto libre", e);
            throw new RecipeImportException(
                    "No pudimos entender la receta de ese texto. Revisa que hayas pegado la receta completa."
            );
        }
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull())
            return null;
        if (v.isInt())
            return v.asInt();
        Matcher m = Pattern.compile("\\d+").matcher(v.asString());
        return m.find() ? Integer.parseInt(m.group()) : null;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asString();
    }

    private String stripFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            t = t.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
        }
        return t;
    }
}