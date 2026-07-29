package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.request.IngredientRequest;
import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.entity.Origin;
import com.example.pinchbackend.entity.Recipe;
import com.example.pinchbackend.entity.User;
import com.example.pinchbackend.exception.AccessDeniedException;
import com.example.pinchbackend.exception.RecipeNotFoundException;
import com.example.pinchbackend.mapper.IngredientMapper;
import com.example.pinchbackend.mapper.RecipeMapper;
import com.example.pinchbackend.repository.RecipeRepository;
import com.example.pinchbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileUploadService fileUploadService;

    private RecipeService recipeService;

    private User author;

    @BeforeEach
    void setUp() {
        RecipeMapper recipeMapper = new RecipeMapper(new IngredientMapper());
        recipeService = new RecipeService(recipeRepository, userRepository, fileUploadService, recipeMapper);

        author = new User();
        author.setId(1);
        author.setEmail("cocinera@pinch.com");
    }

    @Test
    void create_guardaLaRecetaComoManualYDevuelveLaRespuesta() {
        RecipeRequest request = new RecipeRequest();
        request.setTitle("Tortilla");
        request.setTimeMinutes(20);
        request.setServings(2);

        IngredientRequest ing = new IngredientRequest();
        ing.setQty("3");
        ing.setUnit("unidad");
        ing.setName("huevos");
        request.setIngredients(List.of(ing));
        request.setSteps(List.of("Batir", "Cuajar"));

        when(userRepository.findByEmail("cocinera@pinch.com")).thenReturn(Optional.of(author));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        RecipeResponse response = recipeService.create(request, "cocinera@pinch.com");

        assertThat(response.getTitle()).isEqualTo("Tortilla");
        assertThat(response.getOrigin()).isEqualTo(Origin.MANUAL);
        assertThat(response.getIngredients()).hasSize(1);
        assertThat(response.getIngredients().get(0).getName()).isEqualTo("huevos");
        assertThat(response.getSteps()).containsExactly("Batir", "Cuajar");
    }

    @Test
    void getById_cuandoNoEsLaDueña_lanzaAccessDenied() {
        Recipe recipe = new Recipe();
        recipe.setId(5);
        recipe.setAuthor(author);

        when(recipeRepository.findById(5)).thenReturn(Optional.of(recipe));

        assertThatThrownBy(() -> recipeService.getById(5, "otra@pinch.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_cuandoNoExiste_lanzaRecipeNotFound() {
        when(recipeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getById(99, "cocinera@pinch.com"))
                .isInstanceOf(RecipeNotFoundException.class);
    }
}