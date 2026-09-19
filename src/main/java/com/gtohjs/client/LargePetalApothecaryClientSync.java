package com.gtohjs.client;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gtohjs.GTOHJS;
import com.gtohjs.bootstrap.LargePetalApothecaryRecipeTypeRegistration;
import com.gtohjs.util.ModLog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Replaces this recipe type's entries in GTOCore's prebuilt client recipe cache. */
@Mod.EventBusSubscriber(modid = GTOHJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class LargePetalApothecaryClientSync {
    private static final Set<Object> LAST_INJECTED =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private LargePetalApothecaryClientSync() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        try {
            List<GTRecipeDefinition> recipes =
                    LargePetalApothecaryRecipeTypeRegistration.definition()
                            .synchronizeClientRecipes(event.getRecipeManager());
            int injected = replaceGtoClientRecipeCache(recipes);
            ModLog.info("Synchronized large petal apothecary client recipes; " +
                    "categoryRecipes={}, cachedDisplays={}", recipes.size(), injected);
        } catch (Throwable error) {
            // Keep the rest of GTO's client recipe bake intact if its internal cache ABI changes.
            ModLog.error("Large petal apothecary client recipe synchronization failed", error);
        }
    }

    private static synchronized int replaceGtoClientRecipeCache(
            List<GTRecipeDefinition> recipes) throws ReflectiveOperationException {
        Class<?> cacheOwner = Class.forName("com.gtocore.common.data.GTORecipes");
        Field cacheField = cacheOwner.getField("EMI_RECIPES");
        Object currentCache = cacheField.get(null);
        if (!(currentCache instanceof Iterable<?> currentEntries)) {
            throw new IllegalStateException("GTOCore client recipe cache is not initialized");
        }

        LinkedHashSet<Object> merged = new LinkedHashSet<>();
        for (Object entry : currentEntries) {
            if (!LAST_INJECTED.contains(entry)) {
                merged.add(entry);
            }
        }

        Object category = resolveClientCategory(recipes.get(0));
        Constructor<?> displayConstructor = resolveDisplayConstructor();
        Set<Object> injectedEntries = Collections.newSetFromMap(new IdentityHashMap<>());
        for (GTRecipeDefinition recipe : recipes) {
            Object display = displayConstructor.newInstance(recipe, category);
            merged.add(display);
            injectedEntries.add(display);
        }

        cacheField.set(null, ImmutableSet.copyOf(merged));
        LAST_INJECTED.clear();
        LAST_INJECTED.addAll(injectedEntries);
        return injectedEntries.size();
    }

    @SuppressWarnings("unchecked")
    private static Object resolveClientCategory(GTRecipeDefinition recipe)
            throws ReflectiveOperationException {
        Class<?> categoryClass = Class.forName(
                "com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeEMICategory");
        Field categoriesField = categoryClass.getField("CATEGORIES");
        Object rawFactory = categoriesField.get(null);
        if (!(rawFactory instanceof Function<?, ?>)) {
            throw new IllegalStateException("GT client category factory has an unexpected type");
        }
        Function<Object, Object> factory = (Function<Object, Object>) rawFactory;
        Object category = factory.apply(recipe.recipeType.getCategory());
        if (category == null) {
            throw new IllegalStateException("GT client category factory returned null");
        }
        return category;
    }

    private static Constructor<?> resolveDisplayConstructor() throws ReflectiveOperationException {
        Class<?> displayClass = Class.forName("com.gtocore.integration.emi.GTEMIRecipe");
        for (Constructor<?> constructor : displayClass.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 2 && parameters[0] == GTRecipeDefinition.class) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("GTOCore GTEMIRecipe(GTRecipeDefinition, category)");
    }
}
