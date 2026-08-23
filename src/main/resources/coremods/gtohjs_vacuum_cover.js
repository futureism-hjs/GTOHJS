var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function initializeCoreMod() {
    return {
        'gtohjs_register_vacuum_cover': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.data.GTOCovers',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                var injected = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].getOpcode() === Opcodes.RETURN) {
                        method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
                            'com/gtohjs/bootstrap/VacuumCoverRegistration',
                            'register',
                            '()V',
                            ASMAPI.MethodType.STATIC
                        ));
                        injected++;
                    }
                }
                if (injected === 0) {
                    throw new Error('GTOHJS could not find RETURN in GTOCovers.<clinit>()V');
                }
                ASMAPI.log('INFO', 'GTOHJS injected vacuum-cover registration into ' +
                    injected + ' GTOCovers.<clinit> return path(s)');
                return method;
            }
        },
        'gtohjs_vacuum_cover_recipe_condition': {
            'target': {
                'type': 'METHOD',
                'class': 'com.gtocore.common.recipe.condition.VacuumCondition',
                'methodName': 'testCondition',
                'methodDesc': '(Lcom/gregtechceu/gtceu/api/recipe/handler/IRecipeHandlerHolder;Lcom/gregtechceu/gtceu/api/recipe/handler/RecipeHandlerUnit;Lcom/gregtechceu/gtceu/api/recipe/GTRecipeDefinition;)Z'
            },
            'transformer': function(method) {
                var nodes = method.instructions.toArray();
                if (nodes.length === 0) {
                    throw new Error('GTOHJS found an empty VacuumCondition.testCondition method');
                }

                var continueOriginal = new LabelNode();
                var prefix = new InsnList();
                prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
                prefix.add(new FieldInsnNode(
                    Opcodes.GETFIELD,
                    'com/gtocore/common/recipe/condition/VacuumCondition',
                    'tier',
                    'I'
                ));
                prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                prefix.add(ASMAPI.buildMethodCall(
                    'com/gtohjs/cover/VacuumCoverSupport',
                    'satisfies',
                    '(ILcom/gregtechceu/gtceu/api/recipe/handler/IRecipeHandlerHolder;)Z',
                    ASMAPI.MethodType.STATIC
                ));
                prefix.add(new JumpInsnNode(Opcodes.IFEQ, continueOriginal));
                prefix.add(new InsnNode(Opcodes.ICONST_1));
                prefix.add(new InsnNode(Opcodes.IRETURN));
                prefix.add(continueOriginal);
                method.instructions.insertBefore(nodes[0], prefix);
                if (method.maxStack < 2) {
                    method.maxStack = 2;
                }
                ASMAPI.log('INFO', 'GTOHJS added vacuum-cover support to VacuumCondition.testCondition');
                return method;
            }
        }
    };
}
