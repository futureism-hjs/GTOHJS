package com.gtohjs.bootstrap;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gtohjs.GTOHJS;
import com.gtohjs.cover.VacuumCoverBehavior;
import com.gtohjs.util.ModLog;
import net.minecraft.resources.ResourceLocation;

/** Registers the vacuum cover while GTCEu's cover registry is still mutable. */
public final class VacuumCoverRegistration {
    public static final ResourceLocation ID = GTOHJS.id("vacuum_cover");

    private static CoverDefinition definition;
    private static State state = State.NOT_STARTED;

    private VacuumCoverRegistration() {
    }

    public static synchronized void register() {
        if (state == State.REGISTERED) {
            return;
        }

        CoverDefinition existing = GTRegistries.COVERS.get(ID);
        if (existing != null) {
            definition = existing;
            state = State.REGISTERED;
            return;
        }

        state = State.REGISTERING;
        definition = new CoverDefinition(
                ID,
                VacuumCoverBehavior::new,
                new SimpleCoverRenderer(GTOHJS.id("block/cover/vacuum_cover")));
        GTRegistries.COVERS.register(ID, definition);

        if (GTRegistries.COVERS.get(ID) != definition) {
            state = State.FAILED;
            throw new IllegalStateException("Vacuum cover registration failed: " + ID);
        }
        state = State.REGISTERED;
        ModLog.info("Registered vacuum cover {} with vacuum tier {}", ID, VacuumCoverBehavior.VACUUM_TIER);
    }

    public static CoverDefinition definition() {
        if (definition == null) {
            throw new IllegalStateException("Vacuum cover definition was requested before native cover registration");
        }
        return definition;
    }

    public static void validateLoaded() {
        if (state != State.REGISTERED || definition == null || GTRegistries.COVERS.get(ID) != definition) {
            throw new IllegalStateException("Vacuum cover is not registered correctly: state=" + state);
        }
    }

    public static State state() {
        return state;
    }

    public enum State {
        NOT_STARTED,
        REGISTERING,
        REGISTERED,
        FAILED
    }
}
