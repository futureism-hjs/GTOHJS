package com.gtohjs.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.GTOHJS;
import com.gtohjs.util.ModLog;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.extension.MANARecipeExtension;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Converts Botania petal-apothecary recipes without a compile-time Botania dependency. */
public final class LargePetalApothecaryRecipeType extends com.gtolib.api.recipe.RecipeType {
    public static final long EU_PER_TICK = 16L;
    public static final int DURATION_TICKS = 100;

    private final net.minecraft.world.item.crafting.RecipeType<?> proxyType;
    private volatile int representativeRecipeCount;
    private volatile int validatedProxyRecipeCount;

    public LargePetalApothecaryRecipeType(
            ResourceLocation id,
            String group,
            net.minecraft.world.item.crafting.RecipeType<?> proxyType) {
        super(id, group, Objects.requireNonNull(proxyType, "proxyType"));
        this.proxyType = proxyType;
    }

    @Override
    protected GTRecipeDefinition toGTrecipe(ResourceLocation id, Recipe<?> recipe) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipe, "recipe");

        var builder = recipeBuilder(rawRecipeId(id));
        for (Ingredient ingredient : recipe.getIngredients()) {
            builder.inputItems(ingredient);
        }

        Ingredient reagent = getReagent(recipe);
        if (reagent.isEmpty()) {
            throw new IllegalStateException("Empty petal-apothecary reagent: " + id);
        }
        builder.inputItems(reagent);

        RegistryAccess registryAccess =
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        ItemStack output = recipe.getResultItem(registryAccess);
        if (output == null || output.isEmpty()) {
            throw new IllegalStateException("Empty petal-apothecary output: " + id);
        }

        return builder
                .outputItems(output.copy())
                .EUt(EU_PER_TICK)
                .duration(DURATION_TICKS)
                .build();
    }

    @Override
    public synchronized void buildRepresentativeRecipes() {
        rebuildRepresentativeRecipes(currentRecipeManager());
    }

    public synchronized List<GTRecipeDefinition> synchronizeClientRecipes(RecipeManager manager) {
        rebuildRepresentativeRecipes(Objects.requireNonNull(manager, "manager"));
        var synchronizedRecipes = new ArrayList<>(getRecipesInCategory(category));
        synchronizedRecipes.removeIf(recipe -> recipe.recipeType != this);
        synchronizedRecipes.sort(Comparator.comparing(recipe -> recipe.id.toString()));
        if (synchronizedRecipes.isEmpty()) {
            throw new IllegalStateException("No synchronized petal-apothecary recipes were found");
        }
        return List.copyOf(synchronizedRecipes);
    }

    private void rebuildRepresentativeRecipes(@Nullable RecipeManager manager) {
        removeMainCategoryEntry();
        Set<ResourceLocation> addedIds = new HashSet<>();

        var registeredRecipes = new ArrayList<>(recipes.values());
        registeredRecipes.sort(Comparator.comparing(recipe -> recipe.id.toString()));
        for (GTRecipeDefinition recipe : registeredRecipes) {
            addRepresentativeRecipe(recipe, addedIds);
        }

        if (manager != null) {
            var proxyEntries = new ArrayList<>(getProxyRecipes(manager).entrySet());
            proxyEntries.sort(Map.Entry.comparingByKey(
                    Comparator.comparing(ResourceLocation::toString)));
            for (var entry : proxyEntries) {
                addRepresentativeRecipe(toGTrecipe(entry.getKey(), entry.getValue()), addedIds);
            }
        }

        representativeRecipeCount = addedIds.size();
        super.buildRepresentativeRecipes();
    }

    @Override
    public synchronized void clear() {
        super.clear();
        db = null;
    }

    public synchronized int validateProxyRecipes(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        var proxyEntries = new ArrayList<>(getProxyRecipes(server.getRecipeManager()).entrySet());
        proxyEntries.sort(Map.Entry.comparingByKey(
                Comparator.comparing(ResourceLocation::toString)));
        if (proxyEntries.isEmpty()) {
            throw new IllegalStateException("No Botania petal-apothecary recipes were found");
        }

        int validated = 0;
        for (var entry : proxyEntries) {
            Recipe<?> source = entry.getValue();
            GTRecipeDefinition converted = toGTrecipe(entry.getKey(), source);
            validateConvertedRecipe(entry.getKey(), source, converted);
            validated++;
        }

        validatedProxyRecipeCount = validated;
        rebuildRepresentativeRecipes(server.getRecipeManager());
        int categoryCount = getRecipesInCategory(category).size();
        if (representativeRecipeCount < validated || categoryCount < validated) {
            throw new IllegalStateException("Incomplete petal-apothecary representatives for " +
                    registryName + ": proxy=" + validated + ", tracked=" +
                    representativeRecipeCount + ", category=" + categoryCount);
        }

        ModLog.info("Validated {} Botania petal-apothecary proxy recipes for {}; " +
                        "representatives={}, categoryRecipes={}",
                validated, registryName, representativeRecipeCount, categoryCount);
        return validated;
    }

    public net.minecraft.world.item.crafting.RecipeType<?> proxyType() {
        return proxyType;
    }

    public int representativeRecipeCount() {
        return representativeRecipeCount;
    }

    public int validatedProxyRecipeCount() {
        return validatedProxyRecipeCount;
    }

    private void addRepresentativeRecipe(
            GTRecipeDefinition recipe,
            Set<ResourceLocation> addedIds) {
        if (recipe == null || recipe.id == null) {
            throw new IllegalStateException("Null representative petal-apothecary recipe");
        }
        if (addedIds.add(recipe.id)) {
            addToMainCategory(recipe);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeMainCategoryEntry() {
        try {
            Method getter = getClass().getMethod("getCategoryMap");
            Object rawMap = getter.invoke(this);
            if (!(rawMap instanceof Map<?, ?>)) {
                throw new IllegalStateException("GT recipe category map has an unexpected type");
            }
            ((Map<Object, Object>) rawMap).remove(category);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new IllegalStateException("Unable to reset the petal-apothecary category", cause);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Unable to access the GT recipe category map", error);
        }
    }

    private void validateConvertedRecipe(
            ResourceLocation sourceId,
            Recipe<?> source,
            GTRecipeDefinition converted) {
        if (converted == null || converted.recipeType != this) {
            throw new IllegalStateException("Invalid converted petal-apothecary recipe: " + sourceId);
        }
        ResourceLocation expectedId = convertedRecipeId(sourceId);
        if (!expectedId.equals(converted.id)) {
            throw new IllegalStateException("Unexpected converted recipe id for " + sourceId +
                    ": expected=" + expectedId + ", actual=" + converted.id);
        }
        if (converted.eut != EU_PER_TICK || converted.duration != DURATION_TICKS) {
            throw new IllegalStateException("Unexpected power or duration for " + sourceId +
                    ": EUt=" + converted.eut + ", duration=" + converted.duration);
        }

        int expectedInputs = source.getIngredients().size() + 1;
        if (expectedInputs > 17 || converted.itemInputs == null ||
                converted.itemInputs.size() != expectedInputs) {
            throw new IllegalStateException("Unexpected item input count for " + sourceId +
                    ": expected=" + expectedInputs + ", actual=" +
                    (converted.itemInputs == null ? -1 : converted.itemInputs.size()));
        }
        if (converted.itemOutputs == null || converted.itemOutputs.size() != 1 ||
                converted.itemOutputs.get(0).isEmpty()) {
            throw new IllegalStateException("Missing converted item output: " + sourceId);
        }
        if ((converted.fluidInputs != null && !converted.fluidInputs.isEmpty()) ||
                (converted.fluidOutputs != null && !converted.fluidOutputs.isEmpty())) {
            throw new IllegalStateException("Unexpected fluid content in " + sourceId);
        }
        long inputMana = MANARecipeExtension.getInputMANA(converted);
        long outputMana = MANARecipeExtension.getOutputMANA(converted);
        long inputManaPerTick = MANATRecipeExtension.getInputMANAt(converted);
        long outputManaPerTick = MANATRecipeExtension.getOutputMANAt(converted);
        if (inputMana != 0 || outputMana != 0 ||
                inputManaPerTick != 0 || outputManaPerTick != 0) {
            throw new IllegalStateException("Converted petal-apothecary recipe contains " +
                    "MANA/MANAt: " + sourceId + "; MANA=" + inputMana + "/" + outputMana +
                    ", MANAt=" + inputManaPerTick + "/" + outputManaPerTick);
        }
    }

    private ResourceLocation convertedRecipeId(ResourceLocation sourceId) {
        return RecipeBuilder.getTypeID(rawRecipeId(sourceId), this);
    }

    private static ResourceLocation rawRecipeId(ResourceLocation sourceId) {
        return GTOHJS.id(sourceId.getNamespace() + "/" + sourceId.getPath());
    }

    private static Ingredient getReagent(Recipe<?> recipe) {
        ResourceLocation id = recipe.getId();
        try {
            Method method = recipe.getClass().getMethod("getReagent");
            if (method.getParameterCount() != 0 ||
                    !Ingredient.class.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Invalid getReagent signature on " +
                        recipe.getClass().getName());
            }
            Object value = method.invoke(recipe);
            if (!(value instanceof Ingredient reagent) || reagent.isEmpty()) {
                throw new IllegalStateException("Empty petal-apothecary reagent: " + id);
            }
            return reagent;
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new IllegalStateException("getReagent failed for " + id, cause);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Recipe does not expose public getReagent(): " + id,
                    error);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<ResourceLocation, Recipe<?>> getProxyRecipes(RecipeManager manager) {
        Map<ResourceLocation, Recipe<?>> recipesById = new LinkedHashMap<>();
        for (Recipe<?> recipe : (List<Recipe<?>>) (List<?>) manager.getAllRecipesFor(
                (net.minecraft.world.item.crafting.RecipeType) proxyType)) {
            recipesById.put(recipe.getId(), recipe);
        }
        return recipesById;
    }

    @Nullable
    private static RecipeManager currentRecipeManager() {
        if (FMLEnvironment.dist.isClient()) {
            RecipeManager clientManager = ClientAccess.recipeManager();
            if (clientManager != null) {
                return clientManager;
            }
        }

        MinecraftServer server = GTCEu.getMinecraftServer();
        return server == null ? null : server.getRecipeManager();
    }

    /** Kept in a nested class so dedicated servers never load Minecraft client classes. */
    private static final class ClientAccess {
        private ClientAccess() {
        }

        @Nullable
        private static RecipeManager recipeManager() {
            var level = Minecraft.getInstance().level;
            return level == null ? null : level.getRecipeManager();
        }
    }
}
