package mods.flammpfeil.slashblade.bridge.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import java.nio.charset.StandardCharsets;

public class LegacySlashBladeClassTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("SlashBladeLegacyBridge");
    private static final byte[] LEGACY_ROOT_MARKER = "mods/flammpfeil/slashblade/".getBytes(StandardCharsets.UTF_8);
    private final boolean deobfuscatedEnvironment;

    public LegacySlashBladeClassTransformer() {
        this(FMLLaunchHandler.isDeobfuscatedEnvironment());
    }

    LegacySlashBladeClassTransformer(boolean deobfuscatedEnvironment) {
        this.deobfuscatedEnvironment = deobfuscatedEnvironment;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !contains(basicClass, LEGACY_ROOT_MARKER) || isBridgeClass(transformedName)) {
            return basicClass;
        }

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassWriter writer = new ClassWriter(reader, 0);
            reader.accept(new RemappingClassVisitor(writer, deobfuscatedEnvironment), 0);
            return writer.toByteArray();
        } catch (RuntimeException | Error exception) {
            LOGGER.error("Failed to remap legacy SlashBlade references in class {}",
                    transformedName != null ? transformedName : name, exception);
            throw exception;
        }
    }

    private static boolean isBridgeClass(String transformedName) {
        return transformedName != null && transformedName.startsWith("mods.flammpfeil.slashblade.bridge.");
    }

    private static boolean contains(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private static class RemappingClassVisitor extends ClassVisitor {
        private final Remapper remapper;

        RemappingClassVisitor(ClassVisitor visitor, boolean deobfuscatedEnvironment) {
            super(Opcodes.ASM5, visitor);
            this.remapper = new Remapper(deobfuscatedEnvironment);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, remapper.mapInternalName(name), remapper.mapString(signature),
                    remapper.mapInternalName(superName), remapper.mapInternalNames(interfaces));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitAnnotation(remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            super.visitOuterClass(remapper.mapInternalName(owner), name, remapper.mapMethodDesc(desc));
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(remapper.mapInternalName(name), remapper.mapInternalName(outerName), innerName, access);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return new RemappingFieldVisitor(super.visitField(access, name, remapper.mapDesc(desc),
                    remapper.mapString(signature), remapper.mapValue(value)), remapper);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new RemappingMethodVisitor(super.visitMethod(access, remapper.mapMethodName(name, desc),
                    remapper.mapMethodDesc(desc),
                    remapper.mapString(signature), remapper.mapInternalNames(exceptions)), remapper);
        }
    }

    private static class RemappingFieldVisitor extends FieldVisitor {
        private final Remapper remapper;

        RemappingFieldVisitor(FieldVisitor visitor, Remapper remapper) {
            super(Opcodes.ASM5, visitor);
            this.remapper = remapper;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitAnnotation(remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible), remapper);
        }
    }

    private static class RemappingMethodVisitor extends MethodVisitor {
        private final Remapper remapper;

        RemappingMethodVisitor(MethodVisitor visitor, Remapper remapper) {
            super(Opcodes.ASM5, visitor);
            this.remapper = remapper;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return new RemappingAnnotationVisitor(super.visitAnnotationDefault(), remapper);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitAnnotation(remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitParameterAnnotation(parameter, remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            super.visitFrame(type, nLocal, remapper.mapFrameValues(local), nStack, remapper.mapFrameValues(stack));
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, remapper.mapInternalName(type));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, remapper.mapInternalName(owner), name, remapper.mapDesc(desc));
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, remapper.mapInternalName(owner), remapper.mapMethodName(name, desc),
                    remapper.mapMethodDesc(desc), itf);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            super.visitInvokeDynamicInsn(name, remapper.mapMethodDesc(desc), remapper.mapHandle(bsm), remapper.mapValues(bsmArgs));
        }

        @Override
        public void visitLdcInsn(Object cst) {
            super.visitLdcInsn(remapper.mapValue(cst));
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            super.visitMultiANewArrayInsn(remapper.mapDesc(desc), dims);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitInsnAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            super.visitTryCatchBlock(start, end, handler, remapper.mapInternalName(type));
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return new RemappingAnnotationVisitor(super.visitTryCatchAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible), remapper);
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, remapper.mapDesc(desc), remapper.mapString(signature), start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
                                                              Label[] end, int[] index, String desc,
                                                              boolean visible) {
            return new RemappingAnnotationVisitor(super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, remapper.mapDesc(desc), visible), remapper);
        }
    }

    private static class RemappingAnnotationVisitor extends AnnotationVisitor {
        private final Remapper remapper;

        RemappingAnnotationVisitor(AnnotationVisitor visitor, Remapper remapper) {
            super(Opcodes.ASM5, visitor);
            this.remapper = remapper;
        }

        @Override
        public void visit(String name, Object value) {
            super.visit(name, remapper.mapValue(value));
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            super.visitEnum(name, remapper.mapDesc(desc), value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return new RemappingAnnotationVisitor(super.visitAnnotation(name, remapper.mapDesc(desc)), remapper);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new RemappingAnnotationVisitor(super.visitArray(name), remapper);
        }
    }

    private static class Remapper {
        private static final String SET_TRANSLATION_KEY_DESC = "(Ljava/lang/String;)Lnet/minecraft/item/Item;";
        private static final String SET_MAX_DAMAGE_DESC = "(I)Lnet/minecraft/item/Item;";
        private static final String IS_IN_CREATIVE_TAB_DESC = "(Lnet/minecraft/creativetab/CreativeTabs;)Z";
        private static final String GET_SUB_ITEMS_DESC =
                "(Lnet/minecraft/creativetab/CreativeTabs;Lnet/minecraft/util/NonNullList;)V";
        private final boolean deobfuscatedEnvironment;

        Remapper(boolean deobfuscatedEnvironment) {
            this.deobfuscatedEnvironment = deobfuscatedEnvironment;
        }

        private String mapString(String value) {
            if (value == null) {
                return null;
            }

            String mapped = value;
            mapped = mapped.replace("mods/flammpfeil/slashblade/ItemSlashBladeNamed", "mods/flammpfeil/slashblade/item/ItemSlashBladeNamed");
            mapped = mapped.replace("mods/flammpfeil/slashblade/ItemSlashBladeDetune", "mods/flammpfeil/slashblade/item/ItemSlashBladeDetune");
            mapped = mapped.replace("mods/flammpfeil/slashblade/ItemSlashBladeWrapper", "mods/flammpfeil/slashblade/item/ItemSlashBladeWrapper");
            mapped = mapped.replace("mods/flammpfeil/slashblade/ItemSlashBlade", "mods/flammpfeil/slashblade/item/ItemSlashBlade");
            mapped = mapped.replace("mods/flammpfeil/slashblade/RecipeAwakeBlade", "mods/flammpfeil/slashblade/item/crafting/RecipeAwakeBlade");
            mapped = mapped.replace("mods/flammpfeil/slashblade/TagPropertyAccessor", "mods/flammpfeil/slashblade/util/TagPropertyAccessor");
            mapped = mapped.replace("mods/flammpfeil/slashblade/core/CoreProxy", "mods/flammpfeil/slashblade/proxy/CoreProxy");
            mapped = mapped.replace("mods/flammpfeil/slashblade/named/", "mods/flammpfeil/slashblade/item/named/");

            mapped = mapped.replace("mods.flammpfeil.slashblade.ItemSlashBladeNamed", "mods.flammpfeil.slashblade.item.ItemSlashBladeNamed");
            mapped = mapped.replace("mods.flammpfeil.slashblade.ItemSlashBladeDetune", "mods.flammpfeil.slashblade.item.ItemSlashBladeDetune");
            mapped = mapped.replace("mods.flammpfeil.slashblade.ItemSlashBladeWrapper", "mods.flammpfeil.slashblade.item.ItemSlashBladeWrapper");
            mapped = mapped.replace("mods.flammpfeil.slashblade.ItemSlashBlade", "mods.flammpfeil.slashblade.item.ItemSlashBlade");
            mapped = mapped.replace("mods.flammpfeil.slashblade.RecipeAwakeBlade", "mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade");
            mapped = mapped.replace("mods.flammpfeil.slashblade.TagPropertyAccessor", "mods.flammpfeil.slashblade.util.TagPropertyAccessor");
            mapped = mapped.replace("mods.flammpfeil.slashblade.core.CoreProxy", "mods.flammpfeil.slashblade.proxy.CoreProxy");
            mapped = mapped.replace("mods.flammpfeil.slashblade.named.", "mods.flammpfeil.slashblade.item.named.");
            return mapped;
        }

        private String mapInternalName(String value) {
            return mapString(value);
        }

        private String[] mapInternalNames(String[] values) {
            if (values == null) {
                return null;
            }

            String[] mapped = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                mapped[i] = mapInternalName(values[i]);
            }
            return mapped;
        }

        private String mapDesc(String value) {
            return mapString(value);
        }

        private String mapMethodDesc(String value) {
            return mapString(value);
        }

        private String mapMethodName(String name, String desc) {
            if (!deobfuscatedEnvironment) {
                return name;
            }
            if ("func_77655_b".equals(name) && SET_TRANSLATION_KEY_DESC.equals(desc)) {
                return "setTranslationKey";
            }
            if ("func_77656_e".equals(name) && SET_MAX_DAMAGE_DESC.equals(desc)) {
                return "setMaxDamage";
            }
            if ("func_194125_a".equals(name) && IS_IN_CREATIVE_TAB_DESC.equals(desc)) {
                return "isInCreativeTab";
            }
            if ("func_150895_a".equals(name) && GET_SUB_ITEMS_DESC.equals(desc)) {
                return "getSubItems";
            }
            return name;
        }

        private Object mapValue(Object value) {
            if (value instanceof Type) {
                Type type = (Type) value;
                return Type.getType(mapDesc(type.getDescriptor()));
            }
            if (value instanceof Handle) {
                return mapHandle((Handle) value);
            }
            if (value instanceof String) {
                return mapString((String) value);
            }
            return value;
        }

        private Object[] mapValues(Object[] values) {
            if (values == null) {
                return null;
            }

            Object[] mapped = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                mapped[i] = mapValue(values[i]);
            }
            return mapped;
        }

        private Object[] mapFrameValues(Object[] values) {
            if (values == null) {
                return null;
            }

            Object[] mapped = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                mapped[i] = value instanceof String ? mapInternalName((String) value) : value;
            }
            return mapped;
        }

        private Handle mapHandle(Handle handle) {
            return new Handle(handle.getTag(), mapInternalName(handle.getOwner()),
                    mapMethodName(handle.getName(), handle.getDesc()),
                    mapMethodDesc(handle.getDesc()), handle.isInterface());
        }
    }
}
