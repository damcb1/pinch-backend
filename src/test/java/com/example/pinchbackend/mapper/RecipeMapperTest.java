package com.example.pinchbackend.mapper;

import com.example.pinchbackend.dto.request.IngredientRequest;
import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.dto.response.RecipeSummaryResponse;
import com.example.pinchbackend.entity.Difficulty;
import com.example.pinchbackend.entity.Ingredient;
import com.example.pinchbackend.entity.Origin;
import com.example.pinchbackend.entity.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperTest {

    private RecipeMapper recipeMapper;

    @BeforeEach
    void setUp() {
        recipeMapper = new RecipeMapper(new IngredientMapper());
    }

    private RecipeRequest buildRequest() {
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Tortilla de patatas");
        request.setSourceUrl("https://ejemplo.com/tortilla");
        request.setImageUrl("https://ejemplo.com/img.jpg");
        request.setTimeMinutes(30);
        request.setServings(4);
        request.setCuisine("Española");
        request.setDifficulty(Difficulty.EASY);
        request.setSteps(List.of("Pelar patatas", "Freír", "Cuajar"));

        IngredientRequest ing = new IngredientRequest();
        ing.setQty("4");
        ing.setUnit("unidad");
        ing.setName("patatas");
        request.setIngredients(List.of(ing));

        return request;
    }

    @Test
    void toEntity_copiaTodosLosCamposYLosIngredientes() {
        RecipeRequest request = buildRequest();

        Recipe recipe = recipeMapper.toEntity(request);

        assertThat(recipe.getTitle()).isEqualTo("Tortilla de patatas");
        assertThat(recipe.getSourceUrl()).isEqualTo("https://ejemplo.com/tortilla");
        assertThat(recipe.getImageUrl()).isEqualTo("https://ejemplo.com/img.jpg");
        assertThat(recipe.getTimeMinutes()).isEqualTo(30);
        assertThat(recipe.getServings()).isEqualTo(4);
        assertThat(recipe.getCuisine()).isEqualTo("Española");
        assertThat(recipe.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(recipe.getSteps()).containsExactly("Pelar patatas", "Freír", "Cuajar");

        assertThat(recipe.getIngredients()).hasSize(1);
        Ingredient ing = recipe.getIngredients().get(0);
        assertThat(ing.getName()).isEqualTo("patatas");
        assertThat(ing.getQty()).isEqualTo("4");
        assertThat(ing.getUnit()).isEqualTo("unidad");
        assertThat(ing.getRecipe()).isSameAs(recipe);
    }

    @Test
    void updateEntity_reemplazaLosDatosYLimpiaLosIngredientesAnteriores() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Título viejo");
        recipe.setIngredients(new ArrayList<>());
        Ingredient viejo = new Ingredient();
        viejo.setName("ingrediente viejo");
        recipe.getIngredients().add(viejo);

        recipeMapper.updateEntity(recipe, buildRequest());

        assertThat(recipe.getTitle()).isEqualTo("Tortilla de patatas");
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(recipe.getIngredients().get(0).getName()).isEqualTo("patatas");
    }

    @Test
    void toResponse_mapeaLaEntidadAlDtoDeDetalle() {
        Recipe recipe = recipeMapper.toEntity(buildRequest());
        recipe.setId(7);
        recipe.setOrigin(Origin.MANUAL);

        RecipeResponse response = recipeMapper.toResponse(recipe);

        assertThat(response.getId()).isEqualTo(7);
        assertThat(response.getTitle()).isEqualTo("Tortilla de patatas");
        assertThat(response.getOrigin()).isEqualTo(Origin.MANUAL);
        assertThat(response.getSteps()).containsExactly("Pelar patatas", "Freír", "Cuajar");
        assertThat(response.getIngredients()).hasSize(1);
        assertThat(response.getIngredients().get(0).getName()).isEqualTo("patatas");
    }

    @Test
    void toSummary_mapeaSoloLosCamposDelListado() {
        Recipe recipe = recipeMapper.toEntity(buildRequest());
        recipe.setId(3);
        recipe.setOrigin(Origin.IMPORTED);

        RecipeSummaryResponse summary = recipeMapper.toSummary(recipe);

        assertThat(summary.getId()).isEqualTo(3);
        assertThat(summary.getTitle()).isEqualTo("Tortilla de patatas");
        assertThat(summary.getImageUrl()).isEqualTo("https://ejemplo.com/img.jpg");
        assertThat(summary.getTimeMinutes()).isEqualTo(30);
        assertThat(summary.getServings()).isEqualTo(4);
        assertThat(summary.getCuisine()).isEqualTo("Española");
        assertThat(summary.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(summary.getOrigin()).isEqualTo(Origin.IMPORTED);
    }
}