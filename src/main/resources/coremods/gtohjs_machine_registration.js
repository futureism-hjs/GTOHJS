var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var IincInsnNode = Java.type('org.objectweb.asm.tree.IincInsnNode');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function buildCatalogGTRecipes(recipeIndexLocal) {
    var instructions = new InsnList();
    var check = new LabelNode();
    var end = new LabelNode();

    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'beginGTRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new VarInsnNode(Opcodes.ISTORE, recipeIndexLocal));
    instructions.add(check);
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'gtRecipeCount',
        '()I',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, end));

    // RecipeType.recipeBuilder(...) and RecipeBuilder.save() remain literal
    // instructions in Data.commonInit(), after RecipeFilter.init().
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'gtRecipeType',
        '(I)Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'gtRawId',
        '(I)Lnet/minecraft/resources/ResourceLocation;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'configureGTRecipe',
        '(Lcom/gtolib/api/recipe/RecipeBuilder;I)V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'acceptGTRecipe',
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;I)V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new IincInsnNode(recipeIndexLocal, 1));
    instructions.add(new JumpInsnNode(Opcodes.GOTO, check));
    instructions.add(end);
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'completeGTRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function buildCatalogMaterialRecipes(materialLocal, recipeIndexLocal) {
    var instructions = new InsnList();
    var check = new LabelNode();
    var end = new LabelNode();

    instructions.add(new VarInsnNode(Opcodes.ALOAD, materialLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'beginMaterialRecipes',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)Z',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new JumpInsnNode(Opcodes.IFEQ, end));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new VarInsnNode(Opcodes.ISTORE, recipeIndexLocal));
    instructions.add(check);
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'materialRecipeCount',
        '()I',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, end));

    instructions.add(new VarInsnNode(Opcodes.ALOAD, materialLocal));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'materialRecipeType',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, materialLocal));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'materialRawId',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)Lnet/minecraft/resources/ResourceLocation;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, materialLocal));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'configureMaterialRecipe',
        '(Lcom/gtolib/api/recipe/RecipeBuilder;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, materialLocal));
    instructions.add(new InsnNode(Opcodes.SWAP));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, recipeIndexLocal));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'acceptMaterialRecipe',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;' +
            'Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;I)V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new IincInsnNode(recipeIndexLocal, 1));
    instructions.add(new JumpInsnNode(Opcodes.GOTO, check));
    instructions.add(end);
    return instructions;
}

function buildCustomRecipes(recipeIndexLocal) {
    var instructions = buildCatalogGTRecipes(recipeIndexLocal);
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/api/RecipeSourceCatalog',
        'registerCraftingRecipes',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function initializeCoreMod() {
    return {
        'gtohjs_universal_steam_factory_display_modes': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.steam.BaseSteamMultiblockMachine',
                'methodName': 'addDisplayText',
                'methodDesc': '(Ljava/util/List;)V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.RETURN) {
                        var hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/machine/UniversalSteamFactoryModeSupport',
                            'appendDisplayText',
                            '(Lcom/gtocore/common/machine/multiblock/steam/BaseSteamMultiblockMachine;' +
                                'Ljava/util/List;)V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], hook);
                        injected++;
                    }
                }
                if (injected === 0) {
                    throw new Error('GTOHJS could not find RETURN in BaseSteamMultiblockMachine.addDisplayText');
                }
                if (method.maxStack < 2) {
                    method.maxStack = 2;
                }
                ASMAPI.log('INFO', 'GTOHJS injected universal-steam recipe-mode display');
                return method;
            }
        },
        'gtohjs_universal_steam_factory_mv_recipe_limit': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.steam.BaseSteamMultiblockMachine',
                'methodName': 'getRealRecipe',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/recipe/handler/RecipeHandlerUnit;' +
                    'Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)' +
                    'Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                if (nodes.length === 0) {
                    throw new Error('GTOHJS found an empty BaseSteamMultiblockMachine.getRealRecipe');
                }

                var accepted = new LabelNode();
                var hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 2));
                hook.add(ASMAPI.buildMethodCall(
                    'com/gtohjs/machine/UniversalSteamFactoryModeSupport',
                    'isRecipeWithinMvLimit',
                    '(Lcom/gtocore/common/machine/multiblock/steam/BaseSteamMultiblockMachine;' +
                        'Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)Z',
                    ASMAPI.MethodType.STATIC
                ));
                hook.add(new JumpInsnNode(Opcodes.IFNE, accepted));
                hook.add(new InsnNode(Opcodes.ACONST_NULL));
                hook.add(new InsnNode(Opcodes.ARETURN));
                hook.add(accepted);
                method.instructions.insertBefore(nodes[0], hook);

                var durationLocks = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.ARETURN) {
                        var durationHook = new InsnList();
                        durationHook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        durationHook.add(new InsnNode(Opcodes.SWAP));
                        durationHook.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/machine/UniversalSteamFactoryModeSupport',
                            'lockRecipeDuration',
                            '(Lcom/gtocore/common/machine/multiblock/steam/BaseSteamMultiblockMachine;' +
                                'Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)' +
                                'Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], durationHook);
                        durationLocks++;
                    }
                }
                if (durationLocks === 0) {
                    throw new Error('GTOHJS could not find ARETURN in BaseSteamMultiblockMachine.getRealRecipe');
                }
                if (method.maxStack < 2) {
                    method.maxStack = 2;
                }
                ASMAPI.log('INFO', 'GTOHJS injected universal-steam MV recipe limit and 1t duration lock');
                return method;
            }
        },
        'gtohjs_universal_steam_factory_fancy_ui': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine',
                'methodName': 'createUI',
                'methodDesc': '(Lnet/minecraft/world/entity/player/Player;)Lcom/lowdragmc/lowdraglib/gui/modular/ModularUI;'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                if (nodes.length === 0) {
                    throw new Error('GTOHJS found an empty SteamParallelMultiblockMachine.createUI');
                }
                var passThrough = new LabelNode();
                var hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(ASMAPI.buildMethodCall(
                    'com/gtohjs/machine/UniversalSteamFactoryModeSupport',
                    'createFancyUI',
                    '(Lcom/gregtechceu/gtceu/common/machine/multiblock/steam/SteamParallelMultiblockMachine;' +
                        'Lnet/minecraft/world/entity/player/Player;)Lcom/lowdragmc/lowdraglib/gui/modular/ModularUI;',
                    ASMAPI.MethodType.STATIC
                ));
                hook.add(new InsnNode(Opcodes.DUP));
                hook.add(new JumpInsnNode(Opcodes.IFNULL, passThrough));
                hook.add(new InsnNode(Opcodes.ARETURN));
                hook.add(passThrough);
                hook.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(nodes[0], hook);
                if (method.maxStack < 2) {
                    method.maxStack = 2;
                }
                ASMAPI.log('INFO', 'GTOHJS injected universal-steam Fancy UI adapter');
                return method;
            }
        },
        'gtohjs_after_gto_recipe_types_clinit': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.data.GTORecipeTypes',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;

                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.RETURN) {
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/OneStopRareEarthRecipeTypeRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/LargePetalApothecaryRecipeTypeRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeTypeRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        injected++;
                    }
                }

                if (injected === 0) {
                    throw new Error('GTOHJS could not find RETURN in GTORecipeTypes.<clinit>()V');
                }

                ASMAPI.log('INFO', 'GTOHJS injected recipe-type registration into ' +
                    injected + ' GTORecipeTypes.<clinit> return path(s)');
                return method;
            }
        },
        'gtohjs_advanced_generator_array_fixed_storage_limit': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.generator.GeneratorArrayMachine',
                'methodName': '<init>',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/blockentity/MetaMachineBlockEntity;)V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var matched = 0;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.GETSTATIC &&
                        node.owner === 'com/gtocore/common/machine/multiblock/generator/GeneratorArrayMachine' &&
                        node.name === 'generatorLimit' && node.desc === 'I') {
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                        method.instructions.insert(node, ASMAPI.buildMethodCall(
                            'com/gtohjs/machine/AdvancedGeneratorArraySupport',
                            'resolveLimit',
                            '(Lcom/gregtechceu/gtceu/api/blockentity/MetaMachineBlockEntity;I)I',
                            ASMAPI.MethodType.STATIC
                        ));
                        matched++;
                    }
                }
                if (matched !== 1) {
                    throw new Error('GTOHJS expected one GeneratorArrayMachine.generatorLimit read, found ' + matched);
                }
                if (method.maxStack < 4) {
                    method.maxStack = 4;
                }
                ASMAPI.log('INFO', 'GTOHJS injected the advanced generator array fixed limit');
                return method;
            }
        },
        'gtohjs_advanced_generator_array_fixed_multiplier': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.generator.GeneratorArrayMachine',
                'methodName': 'getRealRecipe',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/recipe/handler/RecipeHandlerUnit;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var matched = 0;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.GETSTATIC &&
                        node.owner === 'com/gtocore/common/machine/multiblock/generator/GeneratorArrayMachine' &&
                        node.name === 'multiply' && node.desc === 'D') {
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                        method.instructions.insert(node, ASMAPI.buildMethodCall(
                            'com/gtohjs/machine/AdvancedGeneratorArraySupport',
                            'resolveMultiplier',
                            '(Lcom/gtocore/common/machine/multiblock/generator/GeneratorArrayMachine;D)D',
                            ASMAPI.MethodType.STATIC
                        ));
                        matched++;
                    }
                }
                if (matched !== 1) {
                    throw new Error('GTOHJS expected one GeneratorArrayMachine.multiply read, found ' + matched);
                }
                if (method.maxStack < 7) {
                    method.maxStack = 7;
                }
                ASMAPI.log('INFO', 'GTOHJS injected the advanced generator array fixed 2x multiplier');
                return method;
            }
        },
        'gtohjs_advanced_generator_array_zero_wireless_loss': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.generator.GeneratorArrayMachine',
                'methodName': 'handleTickRecipe',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)Z'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var setLossCalls = 0;
                var patched = 0;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.INVOKEVIRTUAL &&
                        node.owner === 'com/gtolib/api/wireless/ExtendWirelessEnergyContainer' &&
                        node.name === 'setLoss' && node.desc === '(I)V') {
                        setLossCalls++;
                        if (setLossCalls === 1) {
                            method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                            method.instructions.insertBefore(node, ASMAPI.buildMethodCall(
                                'com/gtohjs/machine/AdvancedGeneratorArraySupport',
                                'resolveAppliedWirelessLoss',
                                '(ILcom/gtocore/common/machine/multiblock/generator/GeneratorArrayMachine;)I',
                                ASMAPI.MethodType.STATIC
                            ));
                            patched++;
                        }
                    }
                }
                if (setLossCalls !== 2 || patched !== 1) {
                    throw new Error('GTOHJS expected two GeneratorArrayMachine wireless setLoss calls and patched exactly one, found calls=' +
                        setLossCalls + ', patched=' + patched);
                }
                if (method.maxStack < 5) {
                    method.maxStack = 5;
                }
                ASMAPI.log('INFO', 'GTOHJS injected zero-loss wireless transfer for the advanced generator array');
                return method;
            }
        },
        'gtohjs_after_gto_machines_clinit': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.data.GTOMachines',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;

                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.RETURN) {
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/UniversalSteamFactoryRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/OneStopRareEarthProcessingPlantRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/HyperdimensionalForgeRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/HyperdimensionalSteamFurnaceRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/HyperdimensionalSmelterRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/HyperdimensionalChemicalFactoryRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/AdvancedGeneratorArrayRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/SteamArrayRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/AdvancedSteamArrayRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/AdvancedAlchemyCauldronRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/LargePetalApothecaryRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/FragmentWorldCollectionMachineRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/ThermalAndIntakeHatchRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        injected++;
                    }
                }

                if (injected === 0) {
                    throw new Error('GTOHJS could not find RETURN in GTOMachines.<clinit>()V');
                }

                ASMAPI.log('INFO', 'GTOHJS injected machine registration into ' +
                    injected + ' GTOMachines.<clinit> return path(s)');
                return method;
            }
        },
        'gtohjs_after_gto_ae_machines_clinit': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.data.machines.GTAEMachines',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;

                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.RETURN) {
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/MEInputAssemblyRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/MESuperPatternBufferRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/MESuperWildcardPatternBufferRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        injected++;
                    }
                }

                if (injected === 0) {
                    throw new Error('GTOHJS could not find RETURN in GTAEMachines.<clinit>()V');
                }

                ASMAPI.log('INFO', 'GTOHJS injected ME input assembly and super pattern buffer registration into ' +
                    injected + ' GTAEMachines.<clinit> return path(s)');
                return method;
            }
        },
        'gtohjs_inside_gto_material_process_ingot': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.data.recipe.generated.GTOMaterialRecipeHandler',
                'methodName': 'processIngot',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                if (nodes.length === 0) {
                    throw new Error('GTOHJS found an empty GTOMaterialRecipeHandler.processIngot()V');
                }
                var injected = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.INVOKESTATIC &&
                        nodes[i].owner === 'com/gtohjs/api/RecipeSourceCatalog' &&
                        nodes[i].name === 'beginMaterialRecipes') {
                        injected++;
                    }
                }
                if (injected !== 0) {
                    throw new Error('GTOHJS found an existing bulk cluster injection in processIngot()V');
                }
                var materialRecipeIndexLocal = method.maxLocals;
                method.maxLocals += 1;
                method.instructions.insertBefore(nodes[0],
                    buildCatalogMaterialRecipes(0, materialRecipeIndexLocal));
                if (method.maxStack < 6) {
                    method.maxStack = 6;
                }
                ASMAPI.log('INFO', 'GTOHJS injected bulk cluster recipe generation into ' +
                    'GTOMaterialRecipeHandler.processIngot(Material)');
                return method;
            }
        },
        'gtohjs_after_recipe_filter_init': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.data.Data',
                'methodName': 'commonInit',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;
                var finalized = 0;
                var recipeIndexLocal = method.maxLocals;
                method.maxLocals += 1;

                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.INVOKESTATIC &&
                        node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
                        node.name === 'init' &&
                        node.desc === '()V') {
                        method.instructions.insert(node, buildCustomRecipes(recipeIndexLocal));
                        injected++;
                    }
                    if (node.getOpcode() === Opcodes.INVOKESTATIC &&
                        node.owner === 'com/gtolib/api/recipe/RecipeBuilder' &&
                        node.name === 'finish' &&
                        node.desc === '()V') {
                        var validations = new InsnList();
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/api/RecipeSourceCatalog',
                            'validateGTFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/api/RecipeSourceCatalog',
                            'validateMaterialFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insert(node, validations);
                        finalized++;
                    }
                }

                if (injected !== 1) {
                    throw new Error('GTOHJS expected one RecipeFilter.init() call, found ' + injected);
                }
                if (finalized !== 1) {
                    throw new Error('GTOHJS expected one RecipeBuilder.finish() call, found ' + finalized);
                }

                if (method.maxStack < 4) {
                    method.maxStack = 4;
                }
                ASMAPI.log('INFO', 'GTOHJS injected method-mode GT/crafting recipe catalog after RecipeFilter.init() ' +
                    'and catalog validation after RecipeBuilder.finish()');
                return method;
            }
        },
        'gtohjs_scroll_gto_pattern_buffer_modes': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine',
                'methodName': 'attachSideTabs',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/gui/fancy/TabsWidget;)V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var newCount = 0;
                var initCount = 0;
                var owner = 'com/gtocore/api/gui/configurators/MultiMachineModeFancyConfigurator';
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.NEW && node.desc === owner) {
                        var duplicate = node.getNext();
                        if (duplicate === null || duplicate.getOpcode() !== Opcodes.DUP) {
                            throw new Error('GTOHJS expected DUP after GTO pattern mode configurator NEW');
                        }
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                        method.instructions.remove(duplicate);
                        method.instructions.remove(node);
                        newCount++;
                    } else if (node.getOpcode() === Opcodes.INVOKESPECIAL &&
                               node.owner === owner &&
                               node.name === '<init>' &&
                               node.desc === '(Ljava/util/List;Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;Ljava/util/function/Consumer;)V') {
                        method.instructions.set(node, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'com/gtohjs/machine/PatternBufferModeSupport',
                            'createConfigurator',
                            '(Lcom/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachine;Ljava/util/List;Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;Ljava/util/function/Consumer;)Lcom/gregtechceu/gtceu/api/gui/fancy/IFancyUIProvider;',
                            false
                        ));
                        initCount++;
                    }
                }
                if (newCount !== 1 || initCount !== 1) {
                    throw new Error('GTOHJS expected one GTO pattern mode configurator construction, found NEW=' +
                        newCount + ', INIT=' + initCount);
                }
                if (method.maxStack < 6) {
                    method.maxStack = 6;
                }
                ASMAPI.log('INFO', 'GTOHJS added the selective five-row scrollable ME pattern mode configurator');
                return method;
            }
        },
        'gtohjs_dynamic_gto_pattern_grid': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachineUIHelperKt',
                'methodName': 'createPatternPageWidget',
                'methodDesc': '(Lcom/gtolib/api/gui/ktflexible/VBoxBuilder;Lcom/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt;ILkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Z)Lcom/gtocore/api/gui/ktflexible/MultiPageVScroll;'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var columns = 0;
                var rows = 0;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.BIPUSH && node.operand === 9) {
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                        method.instructions.insertBefore(node, ASMAPI.buildMethodCall(
                            'com/gtohjs/config/MEPatternBufferConfig',
                            'columnsFor',
                            '(Lcom/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt;)I',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.remove(node);
                        columns++;
                    } else if (node.getOpcode() === Opcodes.BIPUSH && node.operand === 6) {
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                        method.instructions.insertBefore(node, ASMAPI.buildMethodCall(
                            'com/gtohjs/config/MEPatternBufferConfig',
                            'rowsFor',
                            '(Lcom/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt;)I',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.remove(node);
                        rows++;
                    }
                }
                if (columns !== 1 || rows !== 1) {
                    throw new Error('GTOHJS expected one pattern grid column and row constant, found columns=' +
                        columns + ', rows=' + rows);
                }
                if (method.maxStack < 3) {
                    method.maxStack = 3;
                }
                ASMAPI.log('INFO', 'GTOHJS made GTO ME pattern grid columns and rows configurable');
                return method;
            }
        },
        'gtohjs_dynamic_gto_pattern_ui_width': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachineKt',
                'methodName': 'createUIWidget',
                'methodDesc': '()Lcom/lowdragmc/lowdraglib/gui/widget/Widget;'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var widths = 0;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.SIPUSH && node.operand === 176) {
                        method.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                        method.instructions.insertBefore(node, ASMAPI.buildMethodCall(
                            'com/gtohjs/config/MEPatternBufferConfig',
                            'uiWidthFor',
                            '(Lcom/gtocore/common/machine/multiblock/part/ae/MEPatternPartMachineKt;)I',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.remove(node);
                        widths++;
                    }
                }
                if (widths !== 1) {
                    throw new Error('GTOHJS expected one native ME pattern UI width constant, found ' + widths);
                }
                if (method.maxStack < 2) {
                    method.maxStack = 2;
                }
                ASMAPI.log('INFO', 'GTOHJS made the ME super pattern buffer UI width follow its column count');
                return method;
            }
        }
    };
}
