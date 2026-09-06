var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function appendResourceLocation(instructions, namespace, path) {
    instructions.add(new TypeInsnNode(Opcodes.NEW, 'net/minecraft/resources/ResourceLocation'));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new LdcInsnNode(namespace));
    instructions.add(new LdcInsnNode(path));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESPECIAL,
        'net/minecraft/resources/ResourceLocation',
        '<init>',
        '(Ljava/lang/String;Ljava/lang/String;)V',
        false
    ));
}

function appendDustRecipeItemFromOwner(instructions, methodName, materialOwner, materialField, amount) {
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gregtechceu/gtceu/api/data/tag/TagPrefix',
        'dust',
        'Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;'
    ));
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        materialOwner,
        materialField,
        'Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;'
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, amount));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        'com/gregtechceu/gtceu/api/data/chemical/ChemicalHelper',
        'get',
        '(Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)Lnet/minecraft/world/item/ItemStack;',
        false
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        methodName,
        '(Lnet/minecraft/world/item/ItemStack;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
}

function appendDustRecipeItem(instructions, methodName, materialField, amount) {
    appendDustRecipeItemFromOwner(
        instructions,
        methodName,
        'com/gregtechceu/gtceu/common/data/GTMaterials',
        materialField,
        amount
    );
}

function appendGTODustRecipeItem(instructions, methodName, materialField, amount) {
    appendDustRecipeItemFromOwner(
        instructions,
        methodName,
        'com/gtocore/common/data/GTOMaterials',
        materialField,
        amount
    );
}

function appendMaterialRecipeFluid(instructions, methodName, materialField, amount) {
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gregtechceu/gtceu/common/data/GTMaterials',
        materialField,
        'Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;'
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, amount));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        methodName,
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
}

function appendStringRecipeItem(instructions, methodName, itemId, amount) {
    instructions.add(new LdcInsnNode(itemId));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, amount));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        methodName,
        '(Ljava/lang/String;I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
}

function appendRecipePowerAndDuration(instructions, eut, duration) {
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, eut));
    instructions.add(new InsnNode(Opcodes.I2L));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'EUt',
        '(J)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, duration));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'duration',
        '(I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
}

function appendRecipeSaveAndAccept(instructions, owner, methodName) {
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        owner,
        methodName,
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));
}

function beginAssemblerRecipe(rawPath) {
    var instructions = new InsnList();
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gtocore/common/data/GTORecipeTypes',
        'ASSEMBLER_RECIPES',
        'Lcom/gtolib/api/recipe/RecipeType;'
    ));
    appendResourceLocation(instructions, 'gtohjs', rawPath);
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    return instructions;
}

function buildMEInputAssemblyRecipe() {
    var instructions = beginAssemblerRecipe('me_input_assembly');
    appendStringRecipeItem(instructions, 'inputItems', 'gtceu:ev_dual_input_hatch', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'ae2:cable_interface', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'ae2:speed_card', 1);
    appendStringRecipeItem(instructions, 'outputItems', 'gtocore:me_input_assembly', 1);
    appendRecipePowerAndDuration(instructions, 480, 300);
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration',
        'acceptInputAssembly'
    );
    return instructions;
}

function buildMEStockingInputAssemblyRecipe() {
    var instructions = beginAssemblerRecipe('me_stocking_input_assembly');
    appendStringRecipeItem(instructions, 'inputItems', 'gtceu:luv_dual_input_hatch', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'gtocore:me_input_assembly', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'ae2:cable_interface', 4);
    appendStringRecipeItem(instructions, 'inputItems', 'gtceu:luv_conveyor_module', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'gtceu:luv_electric_pump', 1);
    appendStringRecipeItem(instructions, 'inputItems', 'ae2:speed_card', 4);
    appendStringRecipeItem(instructions, 'inputItems', 'gtceu:luv_sensor', 1);
    appendStringRecipeItem(instructions, 'outputItems', 'gtocore:me_stocking_input_assembly', 1);
    appendRecipePowerAndDuration(instructions, 30720, 300);
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration',
        'acceptStockingInputAssembly'
    );
    return instructions;
}

function buildMEInputAssemblyRecipes() {
    var instructions = new InsnList();
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration',
        'beginInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(buildMEInputAssemblyRecipe());
    instructions.add(buildMEStockingInputAssemblyRecipe());
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration',
        'completeInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function beginAssemblyLineRecipe(rawPath) {
    var instructions = new InsnList();
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gtocore/common/data/GTORecipeTypes',
        'ASSEMBLY_LINE_RECIPES',
        'Lcom/gtolib/api/recipe/RecipeType;'
    ));
    appendResourceLocation(instructions, 'gtohjs', rawPath);
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    return instructions;
}

function appendImportedRecipeConfiguration(instructions, methodName) {
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        methodName,
        '(Lcom/gtolib/api/recipe/RecipeBuilder;)V',
        ASMAPI.MethodType.STATIC
    ));
}

function buildImportedLargePetalApothecaryRecipe() {
    var instructions = beginAssemblerRecipe('large_petal_apothecary');
    appendImportedRecipeConfiguration(instructions, 'configureLargePetalApothecary');
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        'acceptLargePetalApothecary'
    );
    return instructions;
}

function buildImportedHyperdimensionalChemicalFactoryRecipe() {
    var instructions = beginAssemblyLineRecipe('hyperdimensional_chemical_factory');
    appendImportedRecipeConfiguration(instructions, 'configureHyperdimensionalChemicalFactory');
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        'acceptHyperdimensionalChemicalFactory'
    );
    return instructions;
}

function buildImportedHyperdimensionalSmelterRecipe() {
    var instructions = beginAssemblyLineRecipe('hyperdimensional_smelter');
    appendImportedRecipeConfiguration(instructions, 'configureHyperdimensionalSmelter');
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        'acceptHyperdimensionalSmelter'
    );
    return instructions;
}

function buildImportedRecipeDirectoryRecipes() {
    var instructions = new InsnList();
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        'beginInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(buildImportedLargePetalApothecaryRecipe());
    instructions.add(buildImportedHyperdimensionalChemicalFactoryRecipe());
    instructions.add(buildImportedHyperdimensionalSmelterRecipe());
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
        'completeInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function buildImportedChemicalRecipe(
    rawPath,
    inputMaterialField,
    outputDustAmount,
    outputFluidMaterialField,
    duration,
    acceptMethod
) {
    var instructions = new InsnList();

    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gtocore/common/data/GTORecipeTypes',
        'CHEMICAL_RECIPES',
        'Lcom/gtolib/api/recipe/RecipeType;'
    ));
    appendResourceLocation(instructions, 'gtohjs', rawPath);
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    appendDustRecipeItem(instructions, 'inputItems', inputMaterialField, 1);
    appendDustRecipeItem(instructions, 'outputItems', 'PlatinumGroupSludge', outputDustAmount);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'NitricAcid', 1000);
    appendMaterialRecipeFluid(instructions, 'outputFluids', outputFluidMaterialField, 1000);

    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 30));
    instructions.add(new InsnNode(Opcodes.I2L));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'EUt',
        '(J)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, duration));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'duration',
        '(I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedChemicalReactorRecipeRegistration',
        acceptMethod,
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));

    return instructions;
}

function buildImportedChemicalRecipes() {
    var instructions = new InsnList();
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedChemicalReactorRecipeRegistration',
        'beginInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(buildImportedChemicalRecipe(
        'platinum_group_sludge_from_tetrahedrite',
        'Tetrahedrite',
        4,
        'SulfuricCopperSolution',
        110,
        'acceptTetrahedrite'
    ));
    instructions.add(buildImportedChemicalRecipe(
        'platinum_group_sludge_from_chalcocite',
        'Chalcocite',
        4,
        'SulfuricCopperSolution',
        110,
        'acceptChalcocite'
    ));
    instructions.add(buildImportedChemicalRecipe(
        'platinum_group_sludge_from_bornite',
        'Bornite',
        4,
        'SulfuricCopperSolution',
        110,
        'acceptBornite'
    ));
    instructions.add(buildImportedChemicalRecipe(
        'platinum_group_sludge_from_cooperite',
        'Cooperite',
        8,
        'SulfuricNickelSolution',
        150,
        'acceptCooperite'
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ImportedChemicalReactorRecipeRegistration',
        'completeInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function appendOneStopRecipeBuilder(instructions, rawPath) {
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/OneStopRareEarthRecipeTypeRegistration',
        'definition',
        '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));
    appendResourceLocation(instructions, 'gtohjs', rawPath);
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
}

function appendOneStopSaveAndAccept(instructions, acceptMethod) {
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/OneStopRareEarthRecipeRegistration',
        acceptMethod,
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));
}

function buildOneStopMonaziteRecipe() {
    var instructions = new InsnList();
    appendOneStopRecipeBuilder(instructions, 'rare_earth_dust_from_monazite');
    appendDustRecipeItem(instructions, 'inputItems', 'Monazite', 20);
    appendDustRecipeItem(instructions, 'inputItems', 'Salt', 2);
    appendDustRecipeItem(instructions, 'inputItems', 'Saltpeter', 40);
    appendDustRecipeItem(instructions, 'outputItems', 'RareEarth', 5);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Acetone', 2000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'NitricAcid', 6000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Water', 4000);
    appendRecipePowerAndDuration(instructions, 1920, 1100);
    appendOneStopSaveAndAccept(instructions, 'acceptMonazite');
    return instructions;
}

function buildOneStopBastnasiteRecipe() {
    var instructions = new InsnList();
    appendOneStopRecipeBuilder(instructions, 'rare_earth_dust_from_bastnasite');
    appendDustRecipeItem(instructions, 'inputItems', 'Bastnasite', 5);
    appendDustRecipeItem(instructions, 'inputItems', 'Saltpeter', 2);
    appendDustRecipeItem(instructions, 'outputItems', 'RareEarth', 4);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'HydrofluoricAcid', 2000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'HydrochloricAcid', 1000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Acetone', 2000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Water', 2000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'NitricAcid', 1800);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Steam', 1000);
    appendRecipePowerAndDuration(instructions, 1920, 1200);
    appendOneStopSaveAndAccept(instructions, 'acceptBastnasite');
    return instructions;
}

function buildOneStopOxidesRecipe() {
    var instructions = new InsnList();
    appendOneStopRecipeBuilder(instructions, 'lanthanum_oxide_dust');
    appendDustRecipeItem(instructions, 'inputItems', 'RareEarth', 6);
    appendGTODustRecipeItem(instructions, 'outputItems', 'LanthanumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'PraseodymiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'NeodymiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'CeriumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'EuropiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'GadoliniumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'SamariumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'TerbiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'DysprosiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'HolmiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'ErbiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'ThuliumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'YtterbiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'LutetiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'ScandiumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'YttriumOxide', 1);
    appendGTODustRecipeItem(instructions, 'outputItems', 'PromethiumOxide', 1);
    appendDustRecipeItem(instructions, 'outputItems', 'SodiumHydroxide', 1);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'NitricAcid', 1000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'Water', 3000);
    appendMaterialRecipeFluid(instructions, 'inputFluids', 'HydrochloricAcid', 6000);
    appendRecipePowerAndDuration(instructions, 1920, 440);
    appendOneStopSaveAndAccept(instructions, 'acceptOxides');
    return instructions;
}

function buildOneStopRareEarthRecipes() {
    var instructions = new InsnList();
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/OneStopRareEarthRecipeRegistration',
        'beginInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(buildOneStopMonaziteRecipe());
    instructions.add(buildOneStopBastnasiteRecipe());
    instructions.add(buildOneStopOxidesRecipe());
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/OneStopRareEarthRecipeRegistration',
        'completeInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function buildPlatinumGroupSludgeElectrolysisRecipe() {
    var instructions = new InsnList();

    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gtocore/common/data/GTORecipeTypes',
        'ELECTROLYZER_RECIPES',
        'Lcom/gtolib/api/recipe/RecipeType;'
    ));
    appendResourceLocation(instructions, 'gtohjs', 'platinum_group_sludge_electrolysis');
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    appendDustRecipeItem(instructions, 'inputItems', 'PlatinumGroupSludge', 36);
    appendDustRecipeItem(instructions, 'outputItems', 'Platinum', 4);
    appendDustRecipeItem(instructions, 'outputItems', 'Palladium', 4);
    appendDustRecipeItem(instructions, 'outputItems', 'Ruthenium', 4);
    appendDustRecipeItem(instructions, 'outputItems', 'Iridium', 4);
    appendDustRecipeItem(instructions, 'outputItems', 'Osmium', 2);
    appendDustRecipeItem(instructions, 'outputItems', 'Rhodium', 3);

    instructions.add(new IntInsnNode(Opcodes.SIPUSH, 2048));
    instructions.add(new InsnNode(Opcodes.I2L));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'EUt',
        '(J)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, 1000));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'duration',
        '(I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/PlatinumGroupSludgeRecipeRegistration',
        'accept',
        '(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));

    return instructions;
}

function buildFragmentWorldRecipe(index) {
    var instructions = new InsnList();

    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeTypeRegistration',
        'definition',
        '()Lcom/gtolib/api/recipe/RecipeType;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, index));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
        'rawId',
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

    // Keep save() inline in GTO's native registration window. The Java helper only
    // configures the already-created builder, matching the verified imported-recipe path.
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new IntInsnNode(Opcodes.SIPUSH, index));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
        'configure',
        '(Lcom/gtolib/api/recipe/RecipeBuilder;I)V',
        ASMAPI.MethodType.STATIC
    ));
    appendRecipeSaveAndAccept(
        instructions,
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
        'accept'
    );
    return instructions;
}

function buildFragmentWorldRecipes() {
    var instructions = new InsnList();
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
        'beginInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    for (var index = 0; index < 254; index++) {
        instructions.add(buildFragmentWorldRecipe(index));
    }
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
        'completeInjectedRegistration',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    return instructions;
}

function buildForgeHammerBulkRecipe() {
    var instructions = new InsnList();
    var end = new LabelNode();

    // GTOMaterialRecipeHandler calls processIngot once per generated ingot.
    // The Java guard only filters materials without a registered dust form;
    // the RecipeBuilder chain itself remains inline in GTO's native window.
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration',
        'isEligible',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)Z',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new JumpInsnNode(Opcodes.IFEQ, end));

    // Keep the material below the builder result for accept(Material, result).
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gtocore/common/data/GTORecipeTypes',
        'CLUSTER_RECIPES',
        'Lcom/gtolib/api/recipe/RecipeType;'
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration',
        'rawId',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)Lnet/minecraft/resources/ResourceLocation;',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeType',
        'recipeBuilder',
        '(Lnet/minecraft/resources/ResourceLocation;)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gregtechceu/gtceu/api/data/tag/TagPrefix',
        'ingot',
        'Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;'
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 64));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'inputItems',
        '(Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;' +
            'Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)' +
            'Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    instructions.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        'com/gregtechceu/gtceu/api/data/tag/TagPrefix',
        'dust',
        'Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;'
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 64));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'outputItems',
        '(Lcom/gregtechceu/gtceu/api/data/tag/TagPrefix;' +
            'Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;I)' +
            'Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));

    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 16));
    instructions.add(new InsnNode(Opcodes.I2L));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'EUt',
        '(J)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration',
        'duration',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;)I',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'duration',
        '(I)Lcom/gtolib/api/recipe/RecipeBuilder;',
        false
    ));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        'com/gtolib/api/recipe/RecipeBuilder',
        'save',
        '()Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;',
        false
    ));
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration',
        'accept',
        '(Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;' +
            'Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(end);
    return instructions;
}

function buildCustomRecipes() {
    var instructions = new InsnList();
    instructions.add(buildPlatinumGroupSludgeElectrolysisRecipe());
    instructions.add(buildFragmentWorldRecipes());
    instructions.add(buildMEInputAssemblyRecipes());
    instructions.add(ASMAPI.buildMethodCall(
        'com/gtohjs/bootstrap/CustomCraftingRecipeRegistration',
        'register',
        '()V',
        ASMAPI.MethodType.STATIC
    ));
    instructions.add(buildImportedRecipeDirectoryRecipes());
    instructions.add(buildImportedChemicalRecipes());
    instructions.add(buildOneStopRareEarthRecipes());
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
                        nodes[i].owner === 'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration' &&
                        nodes[i].name === 'isEligible') {
                        injected++;
                    }
                }
                if (injected !== 0) {
                    throw new Error('GTOHJS found an existing bulk cluster injection in processIngot()V');
                }
                method.instructions.insertBefore(nodes[0], buildForgeHammerBulkRecipe());
                if (method.maxStack < 8) {
                    method.maxStack = 8;
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

                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.getOpcode() === Opcodes.INVOKESTATIC &&
                        node.owner === 'com/gtocore/data/recipe/RecipeFilter' &&
                        node.name === 'init' &&
                        node.desc === '()V') {
                        method.instructions.insert(node, buildCustomRecipes());
                        injected++;
                    }
                    if (node.getOpcode() === Opcodes.INVOKESTATIC &&
                        node.owner === 'com/gtolib/api/recipe/RecipeBuilder' &&
                        node.name === 'finish' &&
                        node.desc === '()V') {
                        var validations = new InsnList();
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/ImportedChemicalReactorRecipeRegistration',
                            'validateFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/OneStopRareEarthRecipeRegistration',
                            'validateFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/ForgeHammerBulkRecipeRegistration',
                            'validateFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/MEInputAssemblyRecipeRegistration',
                            'validateFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/ImportedRecipeDirectoryRegistration',
                            'validateFinalized',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        validations.add(ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/FragmentWorldCollectionRecipeRegistration',
                            'validateFinalized',
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

                if (method.maxStack < 5) {
                    method.maxStack = 5;
                }
                ASMAPI.log('INFO', 'GTOHJS injected native custom recipe builders after RecipeFilter.init() ' +
                    'and finalized validations after RecipeBuilder.finish()');
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
