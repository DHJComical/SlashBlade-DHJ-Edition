package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.BladeStateCodec;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemProudSoul;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by Furia on 2016/05/31.
 */
public class AnvilEventHandler {
    @SubscribeEvent
    public void onAnvil(AnvilUpdateEvent event) {
        if (!(event.getLeft().getItem() instanceof ItemSlashBlade))
            return;
        if (event.getRight() == null)
            return;
        if (!(event.getRight().getItem() instanceof ItemProudSoul))
            return;

        event.setMaterialCost(1);

        ItemStack out = event.getLeft().copy();

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(out);

        int cost = event.getCost();

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(event.getLeft());
        for (Enchantment enchantment1 : map.keySet()) {
            if (enchantment1 == null) continue;

            int level = map.get(enchantment1);

            int baseCost = 0;
            switch (enchantment1.getRarity()) {
                case COMMON:
                    baseCost = 1;
                    break;
                case UNCOMMON:
                    baseCost = 2;
                    break;
                case RARE:
                    baseCost = 3;
                    break;
                case VERY_RARE:
                    baseCost = 4;
            }

            cost += baseCost * level;
        }

        float repairFactor;
        switch (event.getRight().getItemDamage()) {
            case 0:
                cost = Math.max(2, cost);
                repairFactor = 0.4f;
                ItemSlashBlade.ProudSoul.add(tag, 200);
                break;
            case 1:
                cost = Math.max(3, cost);
                repairFactor = 0.6f;
                ItemSlashBlade.ProudSoul.add(tag, 400);
                break;
            case 2:
                cost = Math.max(4, cost);
                repairFactor = 0.7f;
                ItemSlashBlade.ProudSoul.add(tag, 400);
                break;
            case 3:
                cost = Math.max(1, cost);
                repairFactor = 0.2f;
                ItemSlashBlade.ProudSoul.add(tag, 100);
                break;
            default: {
                cost = Math.max(5, cost);

                NBTTagCompound matTag = ItemSlashBlade.getItemTagCompound(event.getRight());

                if (ItemSlashBladeNamed.CurrentItemName.exists(matTag)) {
                    ItemStack targetBlade = SlashBlade.findItemStack(SlashBlade.modid, "slashbladeNamed", 1);
                    if (BladeIdentity.matchesIdentity(out, targetBlade)) {
                        String bladeId = BladeIdentity.getBladeId(matTag);
                        ItemStack sourceBlade = out.copy();
                        ItemStack upgradedBlade = SlashBlade.createBladeStack(bladeId);

                        if (!upgradedBlade.isEmpty()) {
                            out = upgradedBlade.copy();
                            tag = ItemSlashBlade.getItemTagCompound(out);
                            BladeStateCodec.copyPersistentState(sourceBlade, out);
                            BladeStateCodec.copyEnchantments(sourceBlade, out);
                            BladeStateCodec.copyDamage(sourceBlade, out);
                        }

                        tag = ItemSlashBlade.getItemTagCompound(out);
                        BladeStateCodec.copyDefinitionOverrides(matTag, tag);

                        if (event.getLeft().hasDisplayName())
                            out.setStackDisplayName(event.getLeft().getDisplayName());
                    }
                    repairFactor = 1.0f;
                    ItemSlashBlade.ProudSoul.add(tag, 0);
                } else {
                    repairFactor = 1.0f;
                    ItemSlashBlade.ProudSoul.add(tag, 500);
                }

                break;
            }
        }
        event.setCost(cost);

        ItemSlashBlade.RepairCount.add(tag, 1);

        int repair = Math.min(out.getItemDamage(), (int) (out.getMaxDamage() * repairFactor));

        out.setItemDamage(out.getItemDamage() - repair);

        if (StringUtils.isBlank(event.getName())){
            if (event.getLeft().hasDisplayName())
                out.clearCustomName();
        }else if (!event.getName().equals(event.getLeft().getDisplayName()))
            out.setStackDisplayName(event.getName());

        event.setOutput(out);
    }

}
