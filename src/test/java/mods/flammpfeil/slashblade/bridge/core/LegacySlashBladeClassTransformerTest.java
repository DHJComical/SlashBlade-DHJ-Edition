package mods.flammpfeil.slashblade.bridge.core;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacySlashBladeClassTransformerTest {
    private static final String NEGORE_ROUSE_JAR = "libs/negorerouse-r5-mc1.12.2.jar";
    private static final String ITEM_NR_SLASH_BLADE =
            "moflop/mods/negorerouse/named/item/ItemNrSlashBlade";
    private static final String ITEM_NR_SLASH_BLADE_ENTRY = ITEM_NR_SLASH_BLADE + ".class";
    private static final String NR_BLADES = "moflop/mods/negorerouse/init/NrBlades";
    private static final String NR_BLADES_ENTRY = NR_BLADES + ".class";

    @Test
    void remapsAllUnresolvedInheritedItemCallsInDeobfuscatedEnvironment() throws IOException {
        LegacySlashBladeClassTransformer transformer = new LegacySlashBladeClassTransformer(true);

        TransformedClass itemClass = transform(transformer, ITEM_NR_SLASH_BLADE, ITEM_NR_SLASH_BLADE_ENTRY);
        TransformedClass bladesClass = transform(transformer, NR_BLADES, NR_BLADES_ENTRY);

        assertEquals("mods/flammpfeil/slashblade/item/ItemSlashBladeNamed", itemClass.superName);
        String getSubItemsDesc =
                "(Lnet/minecraft/creativetab/CreativeTabs;Lnet/minecraft/util/NonNullList;)V";
        String setTranslationKeyDesc = "(Ljava/lang/String;)Lnet/minecraft/item/Item;";
        String isInCreativeTabDesc = "(Lnet/minecraft/creativetab/CreativeTabs;)Z";
        String setMaxDamageDesc = "(I)Lnet/minecraft/item/Item;";

        assertEquals(1, itemClass.countMethod("getSubItems", getSubItemsDesc));
        assertFalse(itemClass.hasMethod("func_150895_a", getSubItemsDesc));
        assertEquals(1, itemClass.countCall("<init>", ITEM_NR_SLASH_BLADE, "setTranslationKey",
                setTranslationKeyDesc));
        assertFalse(itemClass.hasCall("<init>", ITEM_NR_SLASH_BLADE, "func_77655_b",
                setTranslationKeyDesc));
        assertEquals(1, itemClass.countCall("getSubItems", ITEM_NR_SLASH_BLADE, "isInCreativeTab",
                isInCreativeTabDesc));
        assertFalse(itemClass.hasCall("getSubItems", ITEM_NR_SLASH_BLADE, "func_194125_a",
                isInCreativeTabDesc));
        assertEquals(1, bladesClass.countCall("<clinit>", ITEM_NR_SLASH_BLADE, "setMaxDamage",
                setMaxDamageDesc));
        assertFalse(bladesClass.hasCall("<clinit>", ITEM_NR_SLASH_BLADE, "func_77656_e",
                setMaxDamageDesc));
    }

    @Test
    void preservesSrgItemCallsInObfuscatedEnvironment() throws IOException {
        LegacySlashBladeClassTransformer transformer = new LegacySlashBladeClassTransformer(false);

        TransformedClass itemClass = transform(transformer, ITEM_NR_SLASH_BLADE, ITEM_NR_SLASH_BLADE_ENTRY);
        TransformedClass bladesClass = transform(transformer, NR_BLADES, NR_BLADES_ENTRY);

        assertTrue(itemClass.hasMethod("func_150895_a",
                "(Lnet/minecraft/creativetab/CreativeTabs;Lnet/minecraft/util/NonNullList;)V"));
        assertTrue(itemClass.hasCall("<init>", ITEM_NR_SLASH_BLADE, "func_77655_b",
                "(Ljava/lang/String;)Lnet/minecraft/item/Item;"));
        assertTrue(itemClass.hasCall("func_150895_a", ITEM_NR_SLASH_BLADE, "func_194125_a",
                "(Lnet/minecraft/creativetab/CreativeTabs;)Z"));
        assertTrue(bladesClass.hasCall("<clinit>", ITEM_NR_SLASH_BLADE, "func_77656_e",
                "(I)Lnet/minecraft/item/Item;"));
    }

    private static TransformedClass transform(LegacySlashBladeClassTransformer transformer, String className,
                                              String entryName) throws IOException {
        byte[] originalClass = readJarEntry(entryName);
        byte[] transformedClass = transformer.transform(className.replace('/', '.'),
                className.replace('/', '.'), originalClass);
        return TransformedClass.read(transformedClass);
    }

    private static byte[] readJarEntry(String entryName) throws IOException {
        try (JarFile jarFile = new JarFile(Paths.get(NEGORE_ROUSE_JAR).toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryName);
            if (entry == null) {
                throw new IOException("Missing class entry " + entryName + " in " + NEGORE_ROUSE_JAR);
            }
            try (InputStream input = jarFile.getInputStream(entry);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                return output.toByteArray();
            }
        }
    }

    private static final class TransformedClass extends ClassVisitor {
        private final List<MethodCall> calls = new ArrayList<>();
        private final List<MethodSignature> methods = new ArrayList<>();
        private String superName;

        private TransformedClass() {
            super(Opcodes.ASM5);
        }

        static TransformedClass read(byte[] classBytes) {
            TransformedClass transformedClass = new TransformedClass();
            new ClassReader(classBytes).accept(transformedClass, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return transformedClass;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                          String[] interfaces) {
            this.superName = superName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                         String[] exceptions) {
            methods.add(new MethodSignature(name, desc));
            return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String calledName, String calledDesc,
                                            boolean itf) {
                    calls.add(new MethodCall(name, owner, calledName, calledDesc));
                }
            };
        }

        boolean hasCall(String containingMethod, String owner, String name, String desc) {
            return calls.contains(new MethodCall(containingMethod, owner, name, desc));
        }

        int countCall(String containingMethod, String owner, String name, String desc) {
            MethodCall expected = new MethodCall(containingMethod, owner, name, desc);
            int count = 0;
            for (MethodCall call : calls) {
                if (expected.equals(call)) {
                    count++;
                }
            }
            return count;
        }

        boolean hasMethod(String name, String desc) {
            return methods.contains(new MethodSignature(name, desc));
        }

        int countMethod(String name, String desc) {
            MethodSignature expected = new MethodSignature(name, desc);
            int count = 0;
            for (MethodSignature method : methods) {
                if (expected.equals(method)) {
                    count++;
                }
            }
            return count;
        }
    }

    private static final class MethodSignature {
        private final String name;
        private final String desc;

        private MethodSignature(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MethodSignature)) {
                return false;
            }
            MethodSignature that = (MethodSignature) object;
            return name.equals(that.name) && desc.equals(that.desc);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + desc.hashCode();
        }
    }

    private static final class MethodCall {
        private final String containingMethod;
        private final String owner;
        private final String name;
        private final String desc;

        private MethodCall(String containingMethod, String owner, String name, String desc) {
            this.containingMethod = containingMethod;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MethodCall)) {
                return false;
            }
            MethodCall that = (MethodCall) object;
            return containingMethod.equals(that.containingMethod)
                    && owner.equals(that.owner)
                    && name.equals(that.name)
                    && desc.equals(that.desc);
        }

        @Override
        public int hashCode() {
            int result = containingMethod.hashCode();
            result = 31 * result + owner.hashCode();
            result = 31 * result + name.hashCode();
            return 31 * result + desc.hashCode();
        }
    }
}
