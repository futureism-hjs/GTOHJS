package com.gtohjs.gtl;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferProxyPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEWildcardPatternBufferPartMachine;
import com.gtohjs.machine.MESuperWildcardPatternBufferPartMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.TreeMap;

/** Checks a native pattern-buffer input unit before the recipe consumes its contents. */
public final class PatternOrderMethods {
    public enum Failure { NONE, UNKNOWN_SLOT, UNAVAILABLE_TYPE, MISSING_RECIPE, WRONG_MODE, WRONG_RECIPE, NO_MATCH, AMBIGUOUS }
    public record Decision(Failure failure) {
        public boolean accepted() { return failure == Failure.NONE; }
    }

    private PatternOrderMethods() {}

    public static Decision check(ElectricMultiblockMachine machine, RecipeHandlerUnit unit,
                                 GTRecipeDefinition recipe) {
        MEPatternBufferPartMachine buffer;
        int slot;
        if (unit.part instanceof MEPatternBufferPartMachine direct) {
            buffer = direct;
            slot = direct.internalRecipeHandler.getSlotHandlers().indexOf(unit);
        } else if (unit.part instanceof MEPatternBufferProxyPartMachine proxy && proxy.getBuffer() != null) {
            buffer = proxy.getBuffer();
            slot = proxy.getRecipeHandlers().indexOf(unit);
        } else {
            return new Decision(Failure.NONE); // native non-pattern inputs keep native behavior
        }
        if (slot < 0 || slot >= buffer.getInternalInventory().length) {
            return new Decision(Failure.UNKNOWN_SLOT);
        }
        if (!GTRecipeType.available(recipe.recipeType, machine.getAvailableRecipeTypes())) {
            return new Decision(Failure.UNAVAILABLE_TYPE);
        }

        GTRecipeType mode = buffer.gto$getRecipeType();
        if (mode == null && buffer instanceof MEWildcardPatternBufferPartMachine wildcard) {
            mode = wildcard.getRecipeType();
        }
        GTRecipeDefinition locked = buffer.getInternalInventory()[slot].recipe;
        ResourceLocation recorded = locked == null ? null : locked.id;
        if (recorded == null) {
            var pattern = buffer.getPatternInventory().getStackInSlot(slot);
            if (!pattern.isEmpty() && pattern.hasTag()) {
                recorded = ResourceLocation.tryParse(pattern.getTag().getString("recipe"));
            }
        }
        // Wildcard buffers synthesize patterns from inputs; ordinary HJS super buffers keep native slot locks.
        boolean searchBased = buffer instanceof MEWildcardPatternBufferPartMachine
                || buffer instanceof MESuperWildcardPatternBufferPartMachine;
        if (mode != null) {
            if (!GTRecipeType.available(recipe.recipeType, mode)) return new Decision(Failure.WRONG_MODE);
            if (!searchBased && recorded != null) {
                GTRecipeDefinition tagged = findRecordedRecipe(recorded);
                if (tagged == null) return new Decision(Failure.MISSING_RECIPE);
                if (!GTRecipeType.available(tagged.recipeType, mode)) return new Decision(Failure.WRONG_MODE);
            }
            return new Decision(Failure.NONE);
        }
        if (!searchBased && recorded != null) {
            GTRecipeDefinition tagged = findRecordedRecipe(recorded);
            if (tagged == null) return new Decision(Failure.MISSING_RECIPE);
            return new Decision(tagged == recipe ? Failure.NONE : Failure.WRONG_RECIPE);
        }

        // Search all native maps against the actual input unit. Returning false visits every candidate.
        Map<ResourceLocation, GTRecipeDefinition> matches = new TreeMap<>();
        for (GTRecipeType type : machine.getAvailableRecipeTypes()) {
            var inputMap = unit.getSearchMap(type);
            if (inputMap.isEmpty()) continue;
            type.search(unit, inputMap, (candidateUnit, candidate) -> {
                if (candidate.registered) matches.put(candidate.id, candidate);
                return false;
            });
            if (matches.size() > 1) return new Decision(Failure.AMBIGUOUS);
        }
        if (matches.isEmpty()) return new Decision(Failure.NO_MATCH);
        return new Decision(matches.values().iterator().next() == recipe ? Failure.NONE : Failure.WRONG_RECIPE);
    }

    private static GTRecipeDefinition findRecordedRecipe(ResourceLocation id) {
        for (GTRecipeType type : GTRegistries.RECIPE_TYPES.values()) {
            GTRecipeDefinition found = type.recipes.get(id);
            if (found != null) return found;
        }
        return null;
    }
}
