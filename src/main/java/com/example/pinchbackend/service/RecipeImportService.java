package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.response.ImportPreviewResponse;
import com.example.pinchbackend.exception.RecipeImportException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor

public class RecipeImportService {
    private final ObjectMapper objectMapper;
    private final RecipeAiService recipeAiService;

    public ImportPreviewResponse importFromUrl(String url) {
        JsonNode recipeNode = fetchRecipeJsonLd(url);
        if (recipeNode == null) {
            throw new RecipeImportException(
                    "No encontramos una receta legible en este enlace. Pega el texto de la receta y la ordenaremos por ti."
            );
        }
        return mapToPreview(recipeNode, url);
    }

    public ImportPreviewResponse importFromText(String text) {
        return recipeAiService.structureFromText(text);
    }

    private JsonNode fetchRecipeJsonLd(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .referrer("https://www.google.com/")
                    .timeout(10000)
                    .get();

            Elements scripts = doc.select("script[type=application/ld+json]");
            for (var script : scripts) {
                JsonNode root = objectMapper.readTree(script.data());
                JsonNode recipe = findRecipeNode(root);
                if (recipe != null) return recipe;
            }
            return null;
        } catch (IOException e) {
            throw new RecipeImportException(
                    "No pudimos leer este enlace. Copia y pega el texto de la receta."
            );
        }
    }

    private JsonNode findRecipeNode(JsonNode node) {
        if (node == null)
            return null;
        if (node.isArray()) {
            for (JsonNode el : node) {
                JsonNode found = findRecipeNode(el);
                if (found != null)
                    return found;
            }
            return null;
        }
        if (node.isObject()) {
            if (isRecipeType(node))
                return node;
            if (node.has("@graph"))
                return findRecipeNode(node.get("@graph"));
        }
        return null;
    }

    private boolean isRecipeType(JsonNode node) {
        JsonNode type = node.get("@type");
        if (type == null)
            return false;
        if (type.isString())
            return type.asString().equalsIgnoreCase("Recipe");
        if (type.isArray()) {
            for (JsonNode t : type) {
                if (t.asString().equalsIgnoreCase("Recipe"))
                    return true;
            }
        }
        return false;
    }

    ImportPreviewResponse mapToPreview(JsonNode r, String sourceUrl) {
        List<String> ingredientLines = extractIngredientLines(r);
        return new ImportPreviewResponse(
                text(r, "name"),
                sourceUrl,
                parseImage(r),
                parseTimeMinutes(r),
                parseServings(r),
                parseCuisine(r),
                recipeAiService.structureIngredients(ingredientLines),
                parseSteps(r)
        );
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v != null && v.isValueNode()) ? v.asString() : null;
    }

    private JsonNode firstOrSelf(JsonNode node) {
        if (node == null)
            return null;
        if (node.isArray())
            return node.size() > 0 ? node.get(0) : null;
        return node;
    }

    private String parseImage(JsonNode r) {
        JsonNode img = firstOrSelf(r.get("image"));
        if (img == null)
            return null;
        return img.isString() ? img.asString() : text(img, "url");
    }

    private Integer parseTimeMinutes(JsonNode r) {
        Integer total = isoToMinutes(text(r, "totalTime"));
        if (total != null)
            return total;
        Integer prep = isoToMinutes(text(r, "prepTime"));
        Integer cook = isoToMinutes(text(r, "cookTime"));
        if (prep == null && cook == null)
            return null;
        return (prep == null ? 0 : prep) + (cook == null ? 0 : cook);
    }

    private Integer isoToMinutes(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return (int) Duration.parse(iso).toMinutes();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer parseServings(JsonNode r) {
        JsonNode y = firstOrSelf(r.get("recipeYield"));
        if (y == null)
            return null;
        Matcher m = Pattern.compile("\\d+").matcher(y.asString());
        return m.find() ? Integer.parseInt(m.group()) : null;
    }

    private String parseCuisine(JsonNode r) {
        JsonNode c = firstOrSelf(r.get("recipeCuisine"));
        return c == null ? null : c.asString();
    }

    private List<String> extractIngredientLines(JsonNode r) {
        List<String> lines = new ArrayList<>();
        JsonNode ing = r.get("recipeIngredient");
        if (ing != null && ing.isArray()) {
            for (JsonNode line : ing) {
                String value = line.asString().trim();
                if (!value.isEmpty()) {
                    lines.add(value);
                }
            }
        }
        return lines;
    }

    private List<String> parseSteps(JsonNode r) {
        List<String> steps = new ArrayList<>();
        JsonNode instr = r.get("recipeInstructions");
        if (instr == null) return steps;
        if (instr.isString()) {
            addStep(steps, instr.asString());
        } else if (instr.isArray()) {
            collectSteps(instr, steps);
        }
        return steps;
    }

    private void collectSteps(JsonNode arr, List<String> steps) {
        for (JsonNode el : arr) {
            if (el.isString()) {
                addStep(steps, el.asString());
            } else if (el.isObject()) {
                String type = el.path("@type").asString("");
                if (type.equalsIgnoreCase("HowToSection")) {
                    collectSteps(el.path("itemListElement"), steps);
                } else {
                    addStep(steps, el.path("text").asString(""));
                }
            }
        }
    }

    private void addStep(List<String> steps, String text) {
        if (text != null && !text.isBlank()) steps.add(text.trim());
    }
}