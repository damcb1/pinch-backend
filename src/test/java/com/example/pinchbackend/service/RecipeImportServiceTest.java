package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.response.ImportPreviewResponse;
import com.example.pinchbackend.dto.response.IngredientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeImportServiceTest {

    @Mock
    private RecipeAiService recipeAiService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private RecipeImportService importService;

    @BeforeEach
    void setUp() {
        importService = new RecipeImportService(objectMapper, recipeAiService);
    }

    @Test
    void mapToPreview_parseaTiempoRacionesCocinaYPasos() {
        String jsonLd = """
            {
              "@type": "Recipe",
              "name": "Pasta al limón",
              "recipeCuisine": "Italiana",
              "totalTime": "PT1H30M",
              "recipeYield": "4 raciones",
              "recipeIngredient": ["200 g de pasta", "1 limón"],
              "recipeInstructions": [
                { "@type": "HowToStep", "text": "Cuece la pasta" },
                { "@type": "HowToStep", "text": "Añade el limón" }
              ]
            }
            """;
        JsonNode recipe = objectMapper.readTree(jsonLd);

        when(recipeAiService.structureIngredients(anyList()))
                .thenReturn(List.of(new IngredientResponse("200", "g", "pasta")));

        ImportPreviewResponse preview = importService.mapToPreview(recipe, "https://blog.com/pasta");

        assertThat(preview.getTitle()).isEqualTo("Pasta al limón");
        assertThat(preview.getTimeMinutes()).isEqualTo(90);
        assertThat(preview.getServings()).isEqualTo(4);
        assertThat(preview.getCuisine()).isEqualTo("Italiana");
        assertThat(preview.getSteps()).containsExactly("Cuece la pasta", "Añade el limón");
        assertThat(preview.getSourceUrl()).isEqualTo("https://blog.com/pasta");
        assertThat(preview.getIngredients()).hasSize(1);         // lo que devolvió la IA mockeada
    }

    @Test
    void mapToPreview_sumaPrepYCookCuandoNoHayTotalTime() {
        String jsonLd = """
            {
              "@type": "Recipe",
              "name": "Bizcocho",
              "prepTime": "PT10M",
              "cookTime": "PT20M",
              "recipeIngredient": ["harina"],
              "recipeInstructions": "Mezcla y hornea"
            }
            """;
        JsonNode recipe = objectMapper.readTree(jsonLd);
        when(recipeAiService.structureIngredients(anyList()))
                .thenReturn(List.of(new IngredientResponse(null, null, "harina")));

        ImportPreviewResponse preview = importService.mapToPreview(recipe, "https://blog.com/bizcocho");

        assertThat(preview.getTimeMinutes()).isEqualTo(30);
        assertThat(preview.getSteps()).containsExactly("Mezcla y hornea");
    }
}