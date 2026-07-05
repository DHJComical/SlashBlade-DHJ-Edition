package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.Reference;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class LegacyRegistryMappingHandler {
    private static final String LEGACY_MOD_ID = "flammpfeil.slashblade";

    @SubscribeEvent
    public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings()) {
            remap(mapping, ForgeRegistries.ITEMS);
        }
    }

    @SubscribeEvent
    public static void onMissingEntityMappings(RegistryEvent.MissingMappings<EntityEntry> event) {
        for (RegistryEvent.MissingMappings.Mapping<EntityEntry> mapping : event.getMappings()) {
            remap(mapping, ForgeRegistries.ENTITIES);
        }
    }

    private static <T extends IForgeRegistryEntry<T>> void remap(RegistryEvent.MissingMappings.Mapping<T> mapping, IForgeRegistry<T> registry) {
        ResourceLocation key = mapping.key;
        if (!LEGACY_MOD_ID.equals(key.getNamespace())) {
            return;
        }

        T replacement = registry.getValue(new ResourceLocation(SlashBlade.modid, key.getPath()));
        if (replacement != null) {
            mapping.remap(replacement);
        }
    }
}
