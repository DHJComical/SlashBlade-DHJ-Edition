package mods.flammpfeil.slashblade.config;

import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.crafting.RecipeCustomBlade;
//import mods.flammpfeil.slashblade.stats.AchievementList;
import mods.flammpfeil.slashblade.util.SlashBladeAchievementCreateEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.stats.Achievement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


/**
 * Created by Furia on 2016/02/10.
 */
public class ConfigCustomBladeManager {
    String[] lines = {};

    static private String escape(String source){
        return String.format("\"%s\"", source.replace("\\","\\\\").replace("\"","\\quot;").replace("\r", "\\r;").replace("\n", "\\n;"));
    }
    static private String unescape(String source){
        return source.replace("\"", "").replace("\\quot;", "\"").replace("\\r;","\r").replace("\\n;","\n").replace("\\\\", "\\");
    }

    public void loadConfig(Configuration config){
        Property propCustomBlade = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "CustomBlade" ,new String[0]);
        lines = propCustomBlade.getStringList();
        propCustomBlade.setShowInGui(false);
    }

    @SubscribeEvent
    public void onSlashBladeAchievementCreateEvent(SlashBladeAchievementCreateEvent event){
        int x = 1;
        int y = 13;
        for(String line : lines){
            if(line == null || line.trim().isEmpty())
                continue;

            line = line.trim();
            if(!hasCustomBladeAsset(line, "texture.png") || !hasCustomBladeAsset(line, "model.obj"))
                continue;

            String key = "custom_"+line;

            ItemStack customBlade = new ItemStack(SlashBlade.bladeNamed,1,0);

            NBTTagCompound tag = new NBTTagCompound();
            customBlade.setTagCompound(tag);

            ItemSlashBladeNamed.setCurrentItemName(tag, key);
            ItemSlashBladeNamed.CustomMaxDamage.set(tag, 50);
            ItemSlashBlade.setBaseAttackModifier(tag, 4 + Item.ToolMaterial.IRON.getAttackDamage());
            ItemSlashBlade.TextureName.set(tag, "custom/"+line+"/texture");
            ItemSlashBlade.ModelName.set(tag, "custom/"+line+"/model");
            ItemSlashBlade.StandbyRenderType.set(tag, 2);

            ItemStack tiny = SlashBlade.getCustomBlade(SlashBlade.TinyBladeSoulStr);
            tiny.setCount(x);

            IRecipe recipe = new RecipeCustomBlade(customBlade,
                    "P  ",
                    " B ",
                    "  S",
                    'S', SlashBlade.getCustomBlade(SlashBlade.SphereBladeSoulStr),
                    'B', new ItemStack(SlashBlade.bladeNamed,1,0),
                    'P', tiny
            ).setMirrored(false);

            SlashBlade.addRecipe(key,recipe);
            SlashBlade.registerDynamicBladeStack(key, customBlade);
/* todo: advancement
            Achievement achievement = AchievementList.registerCraftingAchievement(
                    key, -3 + x++, y, SlashBlade.getCustomBlade(key), AchievementList.getAchievement("noname"));

            AchievementList.setContent(achievement, key);
*/
            ItemSlashBladeNamed.NamedBlades.add(key);
        }
    }

    private boolean hasCustomBladeAsset(String line, String fileName) {
        String resourcePath = "assets/" + SlashBlade.modid + "/model/custom/" + line + "/" + fileName;
        return getClass().getClassLoader().getResource(resourcePath) != null;
    }
}
