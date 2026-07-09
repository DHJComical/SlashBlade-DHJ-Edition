package mods.flammpfeil.slashblade;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffectHandler;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffectRegister;
import mods.flammpfeil.slashblade.config.ConfigManager;
import mods.flammpfeil.slashblade.config.ConfigCustomBladeManager;
import mods.flammpfeil.slashblade.proxy.CoreProxy;
import mods.flammpfeil.slashblade.event.*;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBladeBambooLight;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDynamic;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamedFixed;
import mods.flammpfeil.slashblade.item.ItemProudSoul;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeWrapper;
import mods.flammpfeil.slashblade.item.BladeDefinition;
import mods.flammpfeil.slashblade.item.BladeDefinitionRegistry;
import mods.flammpfeil.slashblade.item.BladeStateCodec;
import mods.flammpfeil.slashblade.item.crafting.RecipeAdjustPos;
import mods.flammpfeil.slashblade.item.crafting.RecipeInstantRepair;
import mods.flammpfeil.slashblade.item.crafting.RecipeWrapBlade;
import mods.flammpfeil.slashblade.config.ConfigEntityListManager;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import mods.flammpfeil.slashblade.ability.*;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.item.TossEventHandler;
import mods.flammpfeil.slashblade.item.named.*;
import mods.flammpfeil.slashblade.item.named.BladeMaterials;
import mods.flammpfeil.slashblade.item.named.event.LoadEvent;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.*;

@Mod(
    name = Reference.MOD_NAME,
    modid = Reference.MOD_ID,
    version = Reference.VERSION,
    guiFactory = "mods.flammpfeil.slashblade.gui.config.ConfigGuiFactory"
)
public class SlashBlade {


    public static final String modname = Reference.MOD_NAME;
    public static final String modid = Reference.MOD_ID;
    public static final String version = Reference.VERSION;
    public static final String legacyModid = "flammpfeil.slashblade";

    public static final String BrokenBladeWhiteStr = "BrokenBladeWhite";

    public static ItemSlashBlade weapon;
    public static ItemSlashBladeDetune bladeWood;
    public static ItemSlashBladeDetune bladeBambooLight;
    public static ItemSlashBladeDetune bladeSilverBambooLight;
    public static ItemSlashBladeDetune bladeWhiteSheath;
    public static ItemSlashBladeNamed bladeNamed;
    public static final Map<String, ItemSlashBlade> FixedBladeItemRegistry = Maps.newLinkedHashMap();
    public static final Map<String, ItemStack> FixedBladePrototypeRegistry = Maps.newLinkedHashMap();

    public static ItemSlashBladeWrapper wrapBlade = null;

    public static ItemProudSoul proudSoul;

    public static Configuration mainConfiguration;
    public static File mainConfigurationFile;

    public static ConfigEntityListManager manager;

    public static boolean SafeDrop = true;
    public static boolean MobSafeDrop = false;

    public static boolean SneakForceLockOn = false;

    public static boolean UseRenderLivingEvent = false;

    public static boolean RenderEnchantEffect = true;
    public static boolean RenderNFCSEffect = true;

    public static boolean FPVNoBlur = true;
    public static boolean FPVOldStryleLike = true;
    public static boolean FPVDisabledSyncPitch = true;

    public static final String ProudSoulStr = "proudsoul";
    public static final String IngotBladeSoulStr = "ingot_bladesoul";
    public static final String SphereBladeSoulStr = "sphere_bladesoul";
    public static final String TinyBladeSoulStr = "tiny_bladesoul";
    public static final String CrystalBladeSoulStr = "crystal_bladesoul";
    public static final String TrapezohedronBladeSoulStr = "trapezohedron_bladesoul";

    public static final SlashBladeTab tab = new SlashBladeTab("slashblade");

    public static final EventBus InitEventBus = new EventBus();

    //ability
    public static JustGuard abilityJustGuard;
    public static StylishRankManager stylishRankManager;
    public static ChargeFloating abilityChargeFloating;
    public static FireResistance abilityFireResistance;
    public static WaterBreathing abilityWaterBreathing;
    public static UntouchableTime abilityUntouchableTime;
    public static AvoidAction abilityAvoidAction;
    public static EnemyStep abilityEnemyStep;
    public static AerialRave abilityAerialRave;
    public static StunManager abilityStun;
    public static ProjectileBarrier abilityProjectileBarrier;

    public static Multimap<String,IRecipe> recipeMultimap = HashMultimap.create();

    public static void addSmelting(String key, ItemStack input, ItemStack output, float xp){
        GameRegistry.addSmelting(input,output,xp);
        recipeMultimap.put(key,new DummySmeltingRecipe(input,output));
    }
    public static void addRecipe(String key, IRecipe value) {
        addRecipe(key, value, value instanceof DummyRecipeBase);
    }
    public static void addRecipe(String key, IRecipe value, boolean isDummy) {
        boolean registered = false;
        if(!isDummy) {
            if(value.getRegistryName() == null)
                value.setRegistryName(new ResourceLocation(value.getGroup()));
            if (isDuplicateLegacyRecipe(value.getRegistryName())) {
                LogManager.getLogger("SlashBlade").info("Skipped duplicate legacy SlashBlade recipe mirror registration: {}", value.getRegistryName());
            } else {
                ForgeRegistries.RECIPES.register(value);
                registered = true;
            }
        }
        recipeMultimap.put(key, value);
        if (!isDummy && !registered) {
            LogManager.getLogger("SlashBlade").debug("Indexed legacy SlashBlade recipe mirror for compatibility: {}", value.getRegistryName());
        }
    }

    private static boolean isDuplicateLegacyRecipe(ResourceLocation registryName) {
        if (registryName == null || !legacyModid.equals(registryName.getNamespace())) {
            return false;
        }

        ResourceLocation canonicalName = new ResourceLocation(modid, registryName.getPath());
        return ForgeRegistries.RECIPES.getValue(canonicalName) != null;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt){
        mainConfiguration = new Configuration(this.mainConfigurationFile = evt.getSuggestedConfigurationFile());

        try {
            mainConfiguration.load();


            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "SneakForceLockOn", SlashBlade.SneakForceLockOn);
                SlashBlade.SneakForceLockOn = prop.getBoolean();
                prop.setShowInGui(true);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "UseRenderLivingEvent", SlashBlade.UseRenderLivingEvent);
                SlashBlade.UseRenderLivingEvent = prop.getBoolean();
                prop.setShowInGui(true);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "EnchantVisualEffect", true);
                SlashBlade.RenderEnchantEffect = prop.getBoolean();
                prop.setShowInGui(true);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "NFCSVisualEffect", true);
                SlashBlade.RenderNFCSEffect = prop.getBoolean();
                prop.setShowInGui(true);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "FPVNoBlur", true);
                SlashBlade.FPVNoBlur = prop.getBoolean();
                prop.setShowInGui(true);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "FPVOldStryleLike", true);
                SlashBlade.FPVOldStryleLike = prop.getBoolean();
                prop.setShowInGui(true);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_CLIENT, "FPVDisabledSyncPitch", true);
                SlashBlade.FPVDisabledSyncPitch = prop.getBoolean();
                prop.setShowInGui(true);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "FastLeavesDecay", false);
                EntityLumberManager.BlockHarvestDropsEventHandler.fastLeavesDecay = prop.getBoolean(false);
                prop.setShowInGui(true);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "SafeDrop", true, "true:bladestand / false:all ways EntityItem drop");
                SafeDrop = prop.getBoolean(true);
                prop.setShowInGui(true);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "MobSafeDrop", MobSafeDrop, "true:bladestand / false:all ways EntityItem drop");
                MobSafeDrop = prop.getBoolean(MobSafeDrop);
                prop.setShowInGui(true);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "RankpointRange", 100);
                prop.setComment("decrement speed factor up 50<def:100<500 down");
                int range = Math.max(50, Math.min(500, prop.getInt()));
                StylishRankManager.setRankRange(range);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "RankpointUpRateTaunt", 150);
                prop.setComment("percentage 1%<def:150%<200%");
                float range = Math.max(1, Math.min(200, prop.getInt()));
                StylishRankManager.AttackTypes.registerAttackType(StylishRankManager.AttackTypes.Noutou, range / 100.0f);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "RankpointUpRate", 100);
                prop.setComment("percentage 1%<def:100%<200%");
                float range = Math.max(1, Math.min(200, prop.getInt()));
                StylishRankManager.setRankRate(range / 100.0f);
            }
            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "WhiffsRankDownRate", 10);
                prop.setComment("rankpoint change factor percentage 0% <= value <= 100% (0% = disable)");
                int value = prop.getInt();
                value = Math.max(0, Math.min(100, value));
                StylishRankManager.whiffsRankDownDisabled = (value == 0);
                StylishRankManager.whiffsRankDownFactor = value / 100.0f;
            }

            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "DamageMultiplier", 100);
                prop.setComment("blade damage multiplier factor 0% <= value <= 1000% (default=100%, 0%=allways1damage)");
                float value = prop.getInt();
                value = Math.max(0, Math.min(value,1000)) / 100.0f;
                DamageLimitter.setFactor(value);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "DamageLimit", -1);
                prop.setComment("blade damage limit -1:Limitless | 0 <= value <= XX (0=allways1damage)");
                DamageLimitter.setLimit(prop.getInt());
            }


            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "DamageAPMultiplier", 100);
                prop.setComment("ArmorPiercing damage multiplier factor 0% <= value <= 1000% (default=100%, 0%=allways1damage)");
                float value = prop.getInt();
                value = Math.max(0, Math.min(value,1000)) / 100.0f;
                ArmorPiercing.setFactor(value);
            }

            {
                Property prop = SlashBlade.mainConfiguration.get("difficulty", "DamageAPLimit", -1);
                prop.setComment("ArmorPiercing damage limit -1:Limitless | 0 <= value <= XX (0=allways1damage)");
                ArmorPiercing.setLimit(prop.getInt());
            }


            MinecraftForge.EVENT_BUS.register(new ConfigManager());
        }
        finally
        {
            mainConfiguration.save();
        }


        proudSoul = (ItemProudSoul)(new ItemProudSoul())
                .setTranslationKey("slashblade.proudsoul")
                .setCreativeTab(tab)
                .setRegistryName("proudsoul");
        ForgeRegistries.ITEMS.register(proudSoul);


        ItemStack itemProudSoul = new ItemStack(proudSoul,1,0);
        itemProudSoul.setRepairCost(-10);
        registerCustomItemStack(ProudSoulStr , itemProudSoul);

        ItemStack itemIngotBladeSoul = new ItemStack(proudSoul,1,1);
        itemIngotBladeSoul.setRepairCost(-25);
        registerCustomItemStack(IngotBladeSoulStr , itemIngotBladeSoul);

        ItemStack itemSphereBladeSoul = new ItemStack(proudSoul,1,2);
        itemSphereBladeSoul.setRepairCost(-50);
        registerCustomItemStack(SphereBladeSoulStr , itemSphereBladeSoul);

        ItemStack itemTinyBladeSoul = new ItemStack(proudSoul,1,3);
        registerCustomItemStack(TinyBladeSoulStr , itemTinyBladeSoul);

        ItemStack itemCrystalBladeSoul = new ItemStack(proudSoul,1,4);
        itemCrystalBladeSoul.setRepairCost(-65);
        registerCustomItemStack(CrystalBladeSoulStr , itemCrystalBladeSoul);

        ItemStack itemTrapezohedronBladeSoul = new ItemStack(proudSoul,1,5);
        itemCrystalBladeSoul.setRepairCost(-80);
        registerCustomItemStack(TrapezohedronBladeSoulStr , itemTrapezohedronBladeSoul);


        ItemStack steelIngot = new ItemStack(proudSoul,1, ItemProudSoul.EnumSoulType.STEEL_INGOT.getMetadata());
        OreDictionary.registerOre("ingotSteel", steelIngot);
        ItemStack ingotSilver = new ItemStack(proudSoul,1, ItemProudSoul.EnumSoulType.SILVER_INGOT.getMetadata());
        OreDictionary.registerOre("ingotSilver", ingotSilver);

        //==================================================================================================================================

        weapon = (ItemSlashBlade)(new ItemSlashBlade(ToolMaterial.IRON, 4 + ToolMaterial.DIAMOND.getAttackDamage()))
                .setRepairMaterial(new ItemStack(Items.IRON_INGOT))
                .setRepairMaterialOreDic("ingotSteel", "nuggetSteel")
                .setTranslationKey("slashblade")
                .setCreativeTab(tab)
                .setRegistryName("slashblade");

        ForgeRegistries.ITEMS.register(weapon);
        registerCoreBladeDefinition(new ItemStack(weapon));

        //==================================================================================================================================

        bladeWood = (ItemSlashBladeDetune)(new ItemSlashBladeDetune(ToolMaterial.WOOD, 4 + ToolMaterial.WOOD.getAttackDamage()))
                .setDestructable(true)
                .setModelTexture(new ResourceLocationRaw("slashblade", "model/wood.png"))
                .setRepairMaterialOreDic("logWood")
                .setMaxDamage(60)
                .setTranslationKey("slashblade.wood")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeWood");
        ForgeRegistries.ITEMS.register(bladeWood);
        registerCoreBladeDefinition(new ItemStack(bladeWood));

        bladeBambooLight = (ItemSlashBladeDetune)(new ItemSlashBladeDetune(ToolMaterial.WOOD, 4 + ToolMaterial.STONE.getAttackDamage()))
                .setDestructable(true)
                .setModelTexture(new ResourceLocationRaw("slashblade", "model/banboo.png"))
                .setRepairMaterialOreDic("bamboo")
                .setMaxDamage(50)
                .setTranslationKey("slashblade.bamboo")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeBambooLight");
        ForgeRegistries.ITEMS.register(bladeBambooLight);
        registerCoreBladeDefinition(new ItemStack(bladeBambooLight));

        bladeSilverBambooLight = (ItemSlashBladeBambooLight)(new ItemSlashBladeBambooLight(ToolMaterial.WOOD, 4 + ToolMaterial.IRON.getAttackDamage()))
                .setDestructable(true)
                .setModelTexture(new ResourceLocationRaw("slashblade", "model/silverbanboo.png"))
                .setRepairMaterialOreDic("bamboo")
                .setMaxDamage(40)
                .setTranslationKey("slashblade.silverbamboo")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeSilverBambooLight");
        ForgeRegistries.ITEMS.register(bladeSilverBambooLight);
        registerCoreBladeDefinition(new ItemStack(bladeSilverBambooLight));

        bladeWhiteSheath = (ItemSlashBladeDetune)(new ItemSlashBladeDetune(ToolMaterial.IRON, 4 + ToolMaterial.IRON.getAttackDamage()))
                .setDestructable(false)
                .setModelTexture(new ResourceLocationRaw("slashblade", "model/white.png"))
                .setRepairMaterial(new ItemStack(Items.IRON_INGOT))
                .setRepairMaterialOreDic("ingotSteel", "nuggetSteel")
                .setMaxDamage(70)
                .setTranslationKey("slashblade.white")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeWhite");
        ForgeRegistries.ITEMS.register(bladeWhiteSheath);
        registerCoreBladeDefinition(new ItemStack(bladeWhiteSheath));



        //==================================================================================================================================

        wrapBlade = (ItemSlashBladeWrapper)(new ItemSlashBladeWrapper(ToolMaterial.IRON))
                .setMaxDamage(40)
                .setTranslationKey("slashblade.wrapper")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeWrapper");
        ForgeRegistries.ITEMS.register(wrapBlade);
        registerCoreBladeDefinition(new ItemStack(wrapBlade));




        bladeNamed = (ItemSlashBladeNamed)(new ItemSlashBladeDynamic(ToolMaterial.IRON, 4.0f))
                .setMaxDamage(40)
                .setTranslationKey("slashblade.named")
                .setCreativeTab(tab)
                .setRegistryName("slashbladeNamed");
        ForgeRegistries.ITEMS.register(bladeNamed);

        manager = new ConfigEntityListManager();

        MinecraftForge.EVENT_BUS.register(manager);

        NetworkManager.init();

        InitEventBus.register(new BladeMaterials());
        InitEventBus.register(new SimpleBlade());

        InitEventBus.register(new Tagayasan());
        InitEventBus.register(new Yamato());
        InitEventBus.register(new Tukumo());

        InitEventBus.register(new Agito());

        InitEventBus.register(new PSSange());
        InitEventBus.register(new PSYasha());
        InitEventBus.register(new BambooMod());
        InitEventBus.register(new Fox());
        InitEventBus.register(new Tizuru());
        InitEventBus.register(new Doutanuki());

        InitEventBus.register(new Koseki());

        ConfigCustomBladeManager ccb = new ConfigCustomBladeManager();
        ccb.loadConfig(mainConfiguration);
        InitEventBus.register(ccb);

        InitEventBus.post(new LoadEvent.PreInitEvent(evt));

        SlashBlade.addRecipe("wrap", new RecipeWrapBlade());

        CoreProxy.proxy.initializeItemRenderer();

        CapabilityMobEffectHandler.register();
    }

    //StatManager statManager;

    @EventHandler
    public void init(FMLInitializationEvent evt){

        SlashBlade.addRecipe("adjust", new RecipeAdjustPos());

        RecipeInstantRepair recipeRepair = new RecipeInstantRepair();
        SlashBlade.addRecipe("repair", recipeRepair);

        //MinecraftForge.EVENT_BUS.register(recipeRepair);

        SlashBlade.InitEventBus.post(new SlashBladeAchievementCreateEvent());


        int entityId = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"Drive"), EntityDrive.class, "Drive", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SummonedSword"), EntitySummonedSword.class, "PhantomSword", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SpearManager"), EntitySpearManager.class, "DirectAttackDummy", entityId++, this, 250, 10, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SummonedSwordBase"), EntitySummonedSwordBase.class, "SummonedSwordBase", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"WitherSword"), EntityWitherSword.class, "WitherSword", entityId++, this, 250, 10, false);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"JudgmentCutManager"), EntityJudgmentCutManager.class, "JudgmentCutManager", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SakuraEndManager"), EntitySakuraEndManager.class, "SakuraEndManager", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"MaximumBetManager"), EntityMaximumBetManager.class, "MaximumBetManager", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SpearManager"), EntitySpearManager.class, "SpearManager", entityId++, this, 250, 10, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"JustGuardManager"), EntityJustGuardManager.class, "JustGuardManager", entityId++, this, 250, 10, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"BladeStand"), EntityBladeStand.class, "BladeStand", entityId++, this, 250, 20, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SummonedBlade"), EntitySummonedBlade.class, "SummonedBlade", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SummonedSwordAirTrickMarker"), EntitySummonedSwordAirTrickMarker.class, "SummonedSwordATM", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"BlisteringSwords"), EntityBlisteringSwords.class, "BlisteringSwords", entityId++, this, 250, 200, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"HeavyRainSwords"), EntityHeavyRainSwords.class, "HeavyRainSwords", entityId++, this, 250, 200, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SpiralSwords"), EntitySpiralSwords.class, "SpiralSwords", entityId++, this, 250, 200, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"StormSwords"), EntityStormSwords.class, "StormSwords", entityId++, this, 250, 200, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"RapidSlashManager"), EntityRapidSlashManager.class, "RapidSlashManager", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"HelmBrakerManager"), EntityHelmBrakerManager.class, "HelmbrakerManager", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"CaliburManager"), EntityCaliburManager.class, "CaliburManager", entityId++, this, 250, 10, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"GrimGrip"), EntityGrimGrip.class, "GrimGrip", entityId++, this, 250, 10, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid,"GrimGripKey"), EntityGrimGripKey.class, "GrimGripKey", entityId++, this, 250, 200, false);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SlashDimension"), EntitySlashDimension.class, "SlashDimension", entityId++, this, 250, 200, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"LumberManager"), EntityLumberManager.class, "LumberManager", entityId++, this, 250, 200, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"StingerManager"), EntityStingerManager.class, "StingerManager", entityId++, this, 250, 10, true);

        EntityRegistry.registerModEntity(new ResourceLocation(modid,"SpinningSword"), EntitySpinningSword.class, "SpinningSword", entityId++, this, 250, 10, true);

        MinecraftForge.EVENT_BUS.register(new EntityLumberManager.BlockHarvestDropsEventHandler());


        MinecraftForge.EVENT_BUS.register(new DropEventHandler());
        MinecraftForge.EVENT_BUS.register(new AnvilEventHandler());

        //MinecraftForge.EVENT_BUS.register(new SlashBladeItemDestroyEventHandler());
        MinecraftForge.EVENT_BUS.register(new TossEventHandler());

        //ability
        abilityJustGuard = new JustGuard();
        MinecraftForge.EVENT_BUS.register(abilityJustGuard);

        abilityChargeFloating = new ChargeFloating();
        MinecraftForge.EVENT_BUS.register(abilityChargeFloating);

        abilityFireResistance = new FireResistance();
        MinecraftForge.EVENT_BUS.register(abilityFireResistance);

        abilityWaterBreathing = new WaterBreathing ();
        MinecraftForge.EVENT_BUS.register(abilityWaterBreathing);

        abilityUntouchableTime = new UntouchableTime();
        MinecraftForge.EVENT_BUS.register(abilityUntouchableTime);

        abilityAvoidAction = new AvoidAction();
        MinecraftForge.EVENT_BUS.register(abilityAvoidAction);

        abilityEnemyStep = new EnemyStep();
        MinecraftForge.EVENT_BUS.register(abilityEnemyStep);

        abilityAerialRave = new AerialRave();
        MinecraftForge.EVENT_BUS.register(abilityAerialRave);

        MinecraftForge.EVENT_BUS.register(new TeleportCanceller());


        stylishRankManager = new StylishRankManager();
        MinecraftForge.EVENT_BUS.register(stylishRankManager);


        abilityStun = new StunManager();
        MinecraftForge.EVENT_BUS.register(abilityStun);

        abilityProjectileBarrier = new ProjectileBarrier();
        MinecraftForge.EVENT_BUS.register(abilityProjectileBarrier);

        MinecraftForge.EVENT_BUS.register(new PlayerDropsEventHandler());

        MinecraftForge.EVENT_BUS.register(new MoveImputHandler());
        MinecraftForge.EVENT_BUS.register(new IllegalActionEnabler());

        InitEventBus.register(new NamedBladeManager());

        MinecraftForge.EVENT_BUS.register(new ClickCanceller());

        MinecraftForge.EVENT_BUS.register(new Taunt());


        MinecraftForge.EVENT_BUS.register(new DamageLimitter());

        //statManager = new StatManager();
        //MinecraftForge.EVENT_BUS.register(statManager);

        /*
        statManager.registerItemStat(weapon, weapon, "SlashBlade");
        statManager.registerItemStat(bladeWood, weapon, "SlashBlade");
        statManager.registerItemStat(bladeBambooLight, weapon, "SlashBlade");
        statManager.registerItemStat(bladeSilverBambooLight, weapon, "SlashBlade");
        statManager.registerItemStat(bladeWhiteSheath, weapon, "SlashBlade");
        statManager.registerItemStat(wrapBlade, weapon, "SlashBlade");
        statManager.registerItemStat(bladeNamed, weapon, "SlashBlade");
*/

        InitEventBus.post(new LoadEvent.InitEvent(evt));

        FMLInterModComms.sendMessage("BetterAchievements", SlashBlade.modname, SlashBlade.getCustomBlade("slashblade.named.yamato"));
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        List<ItemStack> items = OreDictionary.getOres("bamboo");
        if(0 == items.size()){
            ItemStack itemSphereBladeSoul =
                    SlashBlade.findItemStack(modid, SphereBladeSoulStr, 1);

            SlashBlade.addRecipe("sheath", new ShapedOreRecipe(new ResourceLocation(modid,"recipe")
                    ,wrapBlade,
                    "RBL",
                    "CIC",
                    "LBR",
                    'C', Blocks.COAL_BLOCK,
                    'R', Blocks.LAPIS_BLOCK,
                    'B', Blocks.OBSIDIAN,
                    'I', itemSphereBladeSoul,
                    'L', "logWood"));
        }

        InitEventBus.post(new LoadEvent.PostInitEvent(evt));

        EnchantHelper.initEnchantmentList();

        SpecialEffects.init();

        //AchievementList.init();

        CoreProxy.proxy.postInit();

        MinecraftForge.EVENT_BUS.register(ScheduleEntitySpawner.getInstance());

        FMLCommonHandler.instance().resetClientRecipeBook();
    }

/*
    ICommand command;
    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt)
    {
        command = new CommandHandler();
        evt.registerServerCommand(command);
    }
*/

    static public Map<ResourceLocationRaw, ItemStack> BladeRegistry = Maps.newHashMap();

    public static Collection<ItemSlashBlade> getFixedBladeItems() {
        return Collections.unmodifiableCollection(FixedBladeItemRegistry.values());
    }

    public static Collection<BladeDefinition> getBladeDefinitions() {
        return BladeDefinitionRegistry.getDefinitions();
    }

    public static ItemStack registerDynamicBladeStack(String bladeId, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemSlashBlade) {
            BladeStateCodec.ensureBladeState(stack, bladeId);
        }
        registerCustomItemStack(bladeId, stack);
        return stack;
    }

    static public void registerCustomItemStack(String name, ItemStack stack){
        BladeIdentity.ensureRegisteredBladeId(stack, modid, name);
        if (stack.getItem() instanceof ItemSlashBlade) {
            BladeStateCodec.ensureBladeState(stack);

            String bladeId = BladeIdentity.getBladeId(stack);
            String fullKey = new ResourceLocationRaw(modid, name).toString();
            BladeDefinitionRegistry.registerPrototype(name, stack);
            BladeDefinitionRegistry.registerPrototype(fullKey, stack);
            BladeDefinitionRegistry.registerAlias(name, bladeId);
            BladeDefinitionRegistry.registerAlias(fullKey, bladeId);

            boolean isFixedItem = FixedBladeItemRegistry.containsValue(stack.getItem());
            boolean isCanonicalKey = StringUtils.equals(name, bladeId) || StringUtils.equals(fullKey, bladeId);
            if (!isFixedItem && (!BladeDefinitionRegistry.hasDefinition(bladeId) || isCanonicalKey)) {
                BladeDefinitionRegistry.registerDefinition(bladeId, stack, false, true);
            }
        }
        BladeRegistry.put(new ResourceLocationRaw(modid, name),stack);
    }

    static public ItemStack registerFixedBladeStack(String bladeId, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSlashBlade)) {
            registerCustomItemStack(bladeId, stack);
            return stack;
        }

        String resolvedBladeId = BladeIdentity.getBladeId(stack);
        if (resolvedBladeId.isEmpty()) {
            resolvedBladeId = bladeId;
        }

        ItemSlashBlade sourceItem = (ItemSlashBlade) stack.getItem();
        ItemSlashBlade fixedItem = getOrCreateFixedBladeItem(resolvedBladeId, sourceItem, stack);

        ItemStack fixedStack = new ItemStack(fixedItem, stack.getCount(), stack.getMetadata());
        fixedStack.setItemDamage(stack.getItemDamage());
        if (stack.hasTagCompound()) {
            fixedStack.setTagCompound((net.minecraft.nbt.NBTTagCompound) stack.getTagCompound().copy());
        }

        BladeStateCodec.ensureBladeState(fixedStack, resolvedBladeId);
        BladeDefinitionRegistry.registerDefinition(resolvedBladeId, fixedStack, true, false);
        BladeDefinitionRegistry.registerPrototype(resolvedBladeId, fixedStack);
        if (!FixedBladePrototypeRegistry.containsKey(resolvedBladeId) || resolvedBladeId.equals(bladeId)) {
            FixedBladePrototypeRegistry.put(resolvedBladeId, fixedStack.copy());
        }
        registerCustomItemStack(bladeId, fixedStack);
        return fixedStack;
    }

    private static ItemSlashBlade getOrCreateFixedBladeItem(String bladeId, ItemSlashBlade sourceItem, ItemStack sourceStack) {
        if (FixedBladeItemRegistry.containsKey(bladeId)) {
            return FixedBladeItemRegistry.get(bladeId);
        }

        ItemSlashBlade fixedItem;
        float baseAttackModifier = sourceItem.getBaseAttackModifiers(ItemSlashBlade.getItemTagCompound(sourceStack));
        String registryPath = toFixedBladeRegistryPath(bladeId);

        if (sourceItem instanceof ItemSlashBladeWrapper) {
            ItemSlashBladeWrapper wrapperItem = new ItemSlashBladeWrapper(ToolMaterial.IRON);
            wrapperItem.defaultBaseAttackModifier = baseAttackModifier;
            wrapperItem.setMaxDamage(sourceStack.getMaxDamage());
            copyRepairConfig(wrapperItem, sourceItem);
            fixedItem = (ItemSlashBlade) wrapperItem
                    .setTranslationKey(bladeId)
                    .setRegistryName(modid, registryPath);
        } else {
            ItemSlashBladeNamedFixed namedItem = new ItemSlashBladeNamedFixed(ToolMaterial.IRON, baseAttackModifier);
            namedItem.configureFromPrototype(sourceItem, sourceStack);
            copyRepairConfig(namedItem, sourceItem);
            fixedItem = (ItemSlashBlade) namedItem
                    .setTranslationKey(bladeId)
                    .setRegistryName(modid, registryPath);
        }

        ForgeRegistries.ITEMS.register(fixedItem);
        FixedBladeItemRegistry.put(bladeId, fixedItem);
        return fixedItem;
    }

    private static void copyRepairConfig(ItemSlashBlade targetItem, ItemSlashBlade sourceItem) {
        ItemStack repairMaterial = sourceItem.getRepairMaterial();
        if (!repairMaterial.isEmpty()) {
            targetItem.setRepairMaterial(repairMaterial);
        }

        String[] repairOreDic = sourceItem.getRepairMaterialOreDic();
        if (repairOreDic != null && 0 < repairOreDic.length) {
            targetItem.setRepairMaterialOreDic(repairOreDic);
        }
    }

    private static String toFixedBladeRegistryPath(String bladeId) {
        String normalized = bladeId.toLowerCase(Locale.ROOT).replace(':', '.');
        normalized = normalized.replaceAll("[^a-z0-9._/\\-]", "_");
        normalized = normalized.replace('.', '_').replace('/', '_').replace('-', '_');
        return normalized;
    }

    private static void registerCoreBladeDefinition(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName == null) {
            return;
        }

        BladeStateCodec.ensureBladeState(stack, registryName.toString());
        BladeDefinitionRegistry.registerDefinition(registryName.toString(), stack, true, false);
        BladeDefinitionRegistry.registerPrototype(registryName.toString(), stack);
    }

    public static BladeDefinition getBladeDefinition(String bladeId) {
        return BladeDefinitionRegistry.get(bladeId);
    }

    public static String resolveBladeId(ItemStack stack) {
        return BladeIdentity.getBladeId(stack);
    }

    public static void registerBladeAlias(String alias, String bladeId) {
        BladeDefinitionRegistry.registerAlias(alias, bladeId);
    }

    public static ItemStack createBladeStack(String bladeId) {
        if (StringUtils.isBlank(bladeId)) {
            return ItemStack.EMPTY;
        }

        String normalizedBladeId = normalizeLegacyBladeKey(bladeId);
        ItemStack stack = BladeDefinitionRegistry.createBladeStack(normalizedBladeId);
        if (!stack.isEmpty()) {
            return stack;
        }

        if (!StringUtils.equals(normalizedBladeId, bladeId)) {
            stack = BladeDefinitionRegistry.createBladeStack(bladeId);
            if (!stack.isEmpty()) {
                return stack;
            }
        }

        String[] path = normalizedBladeId.split(":", 2);
        if (path.length == 2 && SlashBlade.modid.equals(path[0])) {
            stack = BladeDefinitionRegistry.createBladeStack(path[1]);
            if (!stack.isEmpty()) {
                return stack;
            }
        }

        return getCustomBlade(normalizedBladeId);
    }

    static public ItemStack findItemStack(String modid, String name, int count){
        String resolvedModId = normalizeLegacyModId(modid);
        String resolvedName = normalizeLegacyBladeKey(name);
        ResourceLocationRaw key = new ResourceLocationRaw(resolvedModId, resolvedName);
        ItemStack stack = ItemStack.EMPTY;

        if (SlashBlade.modid.equals(resolvedModId)) {
            stack = BladeDefinitionRegistry.createBladeStack(resolvedName);
            if (stack.isEmpty()) {
                stack = BladeDefinitionRegistry.createBladeStack(key.toString());
            }
        }

        if(stack.isEmpty() && BladeRegistry.containsKey(key)) {
            stack = BladeRegistry.get(key).copy();

        }else if(stack.isEmpty() && SlashBlade.modid.equals(resolvedModId) && FixedBladePrototypeRegistry.containsKey(resolvedName)) {
            stack = FixedBladePrototypeRegistry.get(resolvedName).copy();

        }else if(stack.isEmpty()) {
            Item item = Item.REGISTRY.getObject(key);
            if (item != null)
                stack = new ItemStack(item);

        }

        if (stack.isEmpty() && (!StringUtils.equals(resolvedModId, modid) || !StringUtils.equals(resolvedName, name))) {
            ResourceLocationRaw legacyKey = new ResourceLocationRaw(modid, name);
            if(BladeRegistry.containsKey(legacyKey)) {
                stack = BladeRegistry.get(legacyKey).copy();
            } else {
                Item item = Item.REGISTRY.getObject(legacyKey);
                if (item != null) {
                    stack = new ItemStack(item);
                }
            }
        }

        if(!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemSlashBlade) {
                BladeStateCodec.ensureBladeState(stack);
            } else {
                BladeIdentity.ensureBladeId(stack);
            }
            stack.setCount(count);
        }

        return stack;
    }



    public static ItemStack getCustomBlade(String modid,String name){
        return SlashBlade.findItemStack(modid, name, 1);
    }
    public static ItemStack getCustomBlade(String key){
        String modid;
        String name;
        {
            String str[] = normalizeLegacyBladeKey(key).split(":",2);
            if(str.length == 2){
                modid = str[0];
                name = str[1];
            }else{
                modid = SlashBlade.modid;
                name = key;
            }
        }

        return getCustomBlade(modid,name);
    }

    private static String normalizeLegacyModId(String modId) {
        return legacyModid.equals(modId) ? SlashBlade.modid : modId;
    }

    private static String normalizeLegacyBladeKey(String key) {
        if (StringUtils.isBlank(key)) {
            return key;
        }

        String legacyColonPrefix = legacyModid + ":";
        if (StringUtils.startsWith(key, legacyColonPrefix)) {
            return SlashBlade.modid + ":" + normalizeLegacyBladeKey(key.substring(legacyColonPrefix.length()));
        }

        String legacyDotPrefix = legacyModid + ".";
        if (StringUtils.startsWith(key, legacyDotPrefix)) {
            return SlashBlade.modid + "." + key.substring(legacyDotPrefix.length());
        }

        return key;
    }


    @EventHandler
    public void instanced(FMLConstructionEvent event){
        MinecraftForge.EVENT_BUS.register(new CapabilityMobEffectRegister());
    }
}
