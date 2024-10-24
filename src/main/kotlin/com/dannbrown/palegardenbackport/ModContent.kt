package com.dannbrown.palegardenbackport

import com.dannbrown.deltaboxlib.DeltaboxLib
import com.dannbrown.palegardenbackport.content.block.PaleMossBlock
import com.dannbrown.palegardenbackport.content.block.PaleMossCarpetBlock
import com.dannbrown.palegardenbackport.content.block.PaleVineBlock
import com.dannbrown.palegardenbackport.content.block.PaleVinePlantBlock
import com.dannbrown.palegardenbackport.content.placerTypes.PaleOakFoliagePlacer
import com.dannbrown.palegardenbackport.content.placerTypes.PaleOakTrunkPlacer
import com.dannbrown.palegardenbackport.content.treeDecorator.PaleOakGroundDecorator
import com.dannbrown.palegardenbackport.content.treeDecorator.PaleOakVineDecorator
import com.dannbrown.palegardenbackport.content.treeGrower.PaleOakTreeGrower
import com.dannbrown.palegardenbackport.datagen.ModDatagen
import com.dannbrown.palegardenbackport.init.ModModelLayers
import com.dannbrown.deltaboxlib.content.entity.boat.BaseBoatEntity
import com.dannbrown.deltaboxlib.content.entity.boat.BaseBoatRenderer
import com.dannbrown.deltaboxlib.content.entity.boat.BaseChestBoatEntity
import com.dannbrown.deltaboxlib.content.item.BoatItem
import com.dannbrown.deltaboxlib.lib.LibTags
import com.dannbrown.deltaboxlib.registry.DeltaboxRegistrate
import com.dannbrown.deltaboxlib.registry.datagen.DeltaboxBoatVariants
import com.dannbrown.deltaboxlib.registry.datagen.DeltaboxCompostables
import com.dannbrown.deltaboxlib.registry.datagen.DeltaboxFlowerPots
import com.dannbrown.deltaboxlib.registry.datagen.DeltaboxWandererTrades
import com.dannbrown.deltaboxlib.registry.generators.BlockFamily
import com.dannbrown.deltaboxlib.registry.generators.BlockGenerator
import com.dannbrown.deltaboxlib.registry.generators.CreativeTabGen
import com.dannbrown.deltaboxlib.registry.generators.ItemGen
import com.dannbrown.deltaboxlib.registry.transformers.BlockLootPresets
import com.dannbrown.deltaboxlib.registry.transformers.BlockstatePresets
import com.dannbrown.deltaboxlib.registry.transformers.ItemModelPresets
import com.dannbrown.deltaboxlib.registry.transformers.RecipePresets
import com.tterrag.registrate.util.DataIngredient
import com.tterrag.registrate.util.entry.BlockEntry
import java.util.function.Supplier
import net.minecraft.client.model.BoatModel
import net.minecraft.client.model.ChestBoatModel
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.event.village.WandererTradesEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.DIST
import javax.swing.text.TabSet

@Mod(ModContent.MOD_ID)
class ModContent {
  companion object {
    const val MOD_ID = "palegardenbackport"
    const val NAME = "Pale Garden Backport"
    const val WOOD_NAME = "pale_oak"
    val LOGGER = LogManager.getLogger()
    val REGISTRATE = DeltaboxRegistrate(MOD_ID)

    // ----- Wood Type -----
    val WOOD_TYPE: WoodType = WoodType.register(WoodType(ModContent.MOD_ID + ":${WOOD_NAME}", BlockSetType.OAK))
    // ----- End Wood Type -----

    // ----- Creative Tabs -----
    val TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)
    val CREATIVE_TAB_KEY = "${MOD_ID}_tab"
    val MOD_TAB: RegistryObject<CreativeModeTab> =
      CreativeTabGen(TABS, MOD_ID).createTab(
        CREATIVE_TAB_KEY,
        { ItemStack(WOOD_FAMILY.blocks[BlockFamily.Type.SAPLING]!!.get()) },
        CreativeModeTabs.SPAWN_EGGS,
        { parameters, output ->
          CreativeTabGen.displayAll(REGISTRATE, parameters, output)
        },
        NAME
      )
    // ----- End Creative Tabs -----

    // ----- Blocks -----
    val BLOCKS = BlockGenerator(REGISTRATE)

    val WOOD_FAMILY = BLOCKS.createFamily(WOOD_NAME)
      .color(MapColor.COLOR_ORANGE, MapColor.COLOR_ORANGE)
      .copyFrom { Blocks.OAK_LOG }
      .toolAndTier(BlockTags.MINEABLE_WITH_AXE, null)
      .woodFamily(WOOD_TYPE, PaleOakTreeGrower())

    val PALE_MOSS_BLOCK: BlockEntry<PaleMossBlock> = BLOCKS.create<PaleMossBlock>("pale_moss_block")
      .copyFrom { Blocks.MOSS_BLOCK }
      .blockFactory { p -> PaleMossBlock(p) }
      .color(MapColor.SNOW)
      .blockTags(listOf(BlockTags.DIRT, BlockTags.MOSS_REPLACEABLE, BlockTags.SNIFFER_DIGGABLE_BLOCK, BlockTags.SMALL_DRIPLEAF_PLACEABLE, BlockTags.SNIFFER_EGG_HATCH_BOOST))
      .itemTags(listOf(ItemTags.DIRT))
      .toolAndTier(BlockTags.MINEABLE_WITH_HOE, null)
      .properties { p -> p.strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY) }
      .blockstate(BlockstatePresets.simpleBlock())
      .register()

    val PALE_MOSS_CARPET_BLOCK: BlockEntry<PaleMossCarpetBlock> = BLOCKS.create<PaleMossCarpetBlock>("pale_moss_carpet")
      .copyFrom { Blocks.MOSS_CARPET }
      .blockFactory { p -> PaleMossCarpetBlock(p) }
      .color(MapColor.SNOW)
      .blockTags(listOf(BlockTags.COMBINATION_STEP_SOUND_BLOCKS, BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH, BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH, BlockTags.SWORD_EFFICIENT))
      .toolAndTier(BlockTags.MINEABLE_WITH_HOE, null)
      .properties { p -> p.strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY) }
      .blockstate(BlockstatePresets.simpleCarpetBlock("pale_moss_carpet"))
      .register()

    val PALE_HANGING_MOSS_PLANT: BlockEntry<PaleVinePlantBlock> = BLOCKS.create<PaleVinePlantBlock>("pale_hanging_moss_plant")
      .copyFrom { Blocks.WEEPING_VINES_PLANT }
      .blockFactory { p -> PaleVinePlantBlock({ PALE_HANGING_MOSS.get() },  p) }
      .color(MapColor.SNOW)
      .properties { p -> p.randomTicks().noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY) }
      .blockstate(BlockstatePresets.simpleCrossBlock("pale_hanging_moss"))
      .loot(BlockLootPresets.dropOtherLoot { PALE_HANGING_MOSS.get() })
      .noItem()
      .register()

    val PALE_HANGING_MOSS: BlockEntry<PaleVineBlock> = BLOCKS.create<PaleVineBlock>("pale_hanging_moss")
      .copyFrom { Blocks.WEEPING_VINES }
      .blockFactory { p -> PaleVineBlock({ PALE_HANGING_MOSS_PLANT.get() },  p) }
      .color(MapColor.SNOW)
      .blockTags(listOf(BlockTags.CLIMBABLE, BlockTags.SWORD_EFFICIENT))
      .toolAndTier(BlockTags.MINEABLE_WITH_HOE, null)
      .properties { p -> p.randomTicks().noCollission().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY) }
      .blockstate(BlockstatePresets.simpleCrossBlock("pale_hanging_moss_tip"))
      .transform { t ->
        t.item()
          .model(ItemModelPresets.simpleLayerItem("pale_hanging_moss_tip"))
          .build()
      }
      .register()

    val BLOCK_OF_RESIN = BLOCKS.create<Block>("block_of_resin")
      .storageBlock({ RESIN_CLUMP.get()}, { DataIngredient.ingredient(Ingredient.of(RESIN_CLUMP.get()), LibTags.forgeItemTag("resin")) }, false)
      .properties { p -> p.instabreak().sound(SoundType.AMETHYST) }
      .toolAndTier(BlockTags.MINEABLE_WITH_PICKAXE, null, false)
      .color(MapColor.COLOR_ORANGE)
      .itemTags(listOf(LibTags.modItemTag(MOD_ID, "resin_blocks")))
      .transform { t ->
        t.lang("Block of Resin")
      }
      .register()

    val RESIN_BRICKS = BLOCKS.createFamily("resin")
      .color(MapColor.COLOR_ORANGE, MapColor.COLOR_ORANGE)
      .sharedProps { p -> p.sound(SoundType.AMETHYST) }
      .copyFrom { Blocks.BRICKS }
      .bricksBlockFamily { RESIN_BRICK.get() }

    val CHISELED_RESIN_BRICKS = BLOCKS.create<Block>("chiseled_resin_bricks")
      .itemTags(listOf(LibTags.modItemTag(MOD_ID, "resin_blocks")))
      .recipe { c, p ->
        RecipePresets.slabToChiseledRecipe(c, p) { DataIngredient.items(RESIN_BRICKS.blocks[BlockFamily.Type.BRICK_SLAB]!!.get()) }
        RecipePresets.simpleStonecuttingRecipe(c, p, { DataIngredient.items(RESIN_BRICKS.blocks[BlockFamily.Type.BRICKS]!!.get()) })
      }
      .register()


    // ----- End Blocks -----

    // ----- Items -----
    val ITEMS = ItemGen(REGISTRATE)

    val BOAT = ITEMS.simpleItem("${WOOD_NAME}_boat", { p -> BoatItem(WOOD_NAME, { MOD_BOAT.get() }, false, p.stacksTo(1)) })
    val CHEST_BOAT = ITEMS.simpleItem("${WOOD_NAME}_chest_boat", { p -> BoatItem(WOOD_NAME, { MOD_CHEST_BOAT.get() }, true, p.stacksTo(1)) })
    val RESIN_CLUMP = ITEMS.simpleItem("resin_clump")
    val RESIN_BRICK = ITEMS.simpleItem("resin_brick")
    // ----- End Items -----

    // ----- BlockEntities -----
    // ----- End BlockEntities -----

    // ----- Entities -----
    val ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModContent.MOD_ID)

    val MOD_BOAT: RegistryObject<EntityType<BaseBoatEntity>> =
            ENTITY_TYPES.register("${WOOD_NAME}_boat") {
              EntityType.Builder.of(
                              { pEntityType: EntityType<BaseBoatEntity>, pLevel: Level ->
                                BaseBoatEntity({ BOAT.get() }, { pEntityType }, pLevel)
                              },
                              MobCategory.MISC
                      )
                      .sized(1.375f, 0.5625f)
                      .build("${WOOD_NAME}_boat")
            }

    val MOD_CHEST_BOAT: RegistryObject<EntityType<BaseChestBoatEntity>> =
            ENTITY_TYPES.register("${WOOD_NAME}_chest_boat") {
              EntityType.Builder.of(
                              { pEntityType: EntityType<BaseChestBoatEntity>, pLevel: Level ->
                                BaseChestBoatEntity({ CHEST_BOAT.get() }, { pEntityType }, pLevel)
                              },
                              MobCategory.MISC
                      )
                      .sized(1.375f, 0.5625f)
                      .build("${WOOD_NAME}_chest_boat")
            }
    // ----- End Entities -----

    // ----- Placer Types -----
    val FOLIAGE_PLACER_TYPES = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, MOD_ID)
    val TRUNK_PLACER_TYPES = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, MOD_ID)
    val TREE_DECORATOR_TYPES = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, MOD_ID)

    val GROUND_DECORATOR = TREE_DECORATOR_TYPES.register("pale_oak_ground_decorator") { TreeDecoratorType(PaleOakGroundDecorator.CODEC) }
    val VINE_DECORATOR = TREE_DECORATOR_TYPES.register("pale_oak_vine_decorator") { TreeDecoratorType(PaleOakVineDecorator.CODEC) }

    val PALE_OAK_FOLIAGE_PLACER = FOLIAGE_PLACER_TYPES.register("pale_oak") { FoliagePlacerType(PaleOakFoliagePlacer.CODEC) }
    val PALE_OAK_TRUNK_PLACER = TRUNK_PLACER_TYPES.register("pale_oak") { TrunkPlacerType(PaleOakTrunkPlacer.CODEC) }
    // ----- End Placer Types -----

    // register
    fun register(modBus: IEventBus, forgeEventBus: IEventBus) {
      LOGGER.info("$MOD_ID has started!")

      ModBoatTypes.register()

      ENTITY_TYPES.register(modBus)
      FOLIAGE_PLACER_TYPES.register(modBus)
      TRUNK_PLACER_TYPES.register(modBus)
      TREE_DECORATOR_TYPES.register(modBus)
      TABS.register(modBus)

      REGISTRATE.registerEventListeners(modBus)
      modBus.addListener(::commonSetup)
      modBus.addListener(EventPriority.LOWEST) { event: GatherDataEvent ->
        ModDatagen.gatherData(event)
      }
      forgeEventBus.addListener { event: WandererTradesEvent -> AddonWandererTrades(event).register() }
    }

    fun registerClient(modBus: IEventBus, forgeEventBus: IEventBus) {
      modBus.addListener(::clientSetup)

      modBus.addListener { event: EntityRenderersEvent.RegisterLayerDefinitions ->
        event.registerLayerDefinition(ModModelLayers.BOAT_LAYER, BoatModel::createBodyModel)
        event.registerLayerDefinition(ModModelLayers.CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel)
      }
    }

    // RUN SETUP
    private fun commonSetup(event: FMLCommonSetupEvent) {
      event.enqueueWork {
        // FLOWER POTS
        ModFlowerPots.register()
        // COMPOSTABLES
        ModCompostables.register()
      }
    }

    // Run Client Setup
    private fun clientSetup(event: FMLClientSetupEvent) {
      Sheets.addWoodType(WOOD_TYPE)
      EntityRenderers.register(MOD_BOAT.get()) { pContext: EntityRendererProvider.Context ->
        BaseBoatRenderer(MOD_ID, pContext, false)
      }
      EntityRenderers.register(MOD_CHEST_BOAT.get()) { pContext: EntityRendererProvider.Context ->
        BaseBoatRenderer(MOD_ID, pContext, true)
      }
    }
  }

  init {
    val modBus = FMLJavaModLoadingContext.get().modEventBus
    val forgeEventBus = MinecraftForge.EVENT_BUS
    register(modBus, forgeEventBus)
    // client
    if (DIST.isClient) {
      // register main mod client content
      registerClient(modBus, forgeEventBus)
    }
  }

  // COMPATIBILITIES
  object ModBoatTypes :
          DeltaboxBoatVariants(
                  mapOf(WOOD_NAME to Supplier { WOOD_FAMILY.blocks[BlockFamily.Type.MAIN]!!.get() })
          )

  object ModCompostables :
          DeltaboxCompostables(
                  mutableMapOf(
                          WOOD_FAMILY.blocks[BlockFamily.Type.SAPLING]!!.asItem() to 0.3f,
                          WOOD_FAMILY.blocks[BlockFamily.Type.LEAVES]!!.asItem() to 0.3f,
                          PALE_HANGING_MOSS.asItem() to 0.3f,
                          PALE_MOSS_BLOCK.asItem() to 0.65f,
                          PALE_MOSS_CARPET_BLOCK.asItem() to 0.3f
                  )
          )

  object ModFlowerPots :
          DeltaboxFlowerPots(
                  mutableMapOf(
                          WOOD_FAMILY.blocks[BlockFamily.Type.SAPLING]!! to
                                  WOOD_FAMILY.blocks[BlockFamily.Type.POTTED_SAPLING]!!,
                  )
          )


  class AddonWandererTrades(event: WandererTradesEvent): DeltaboxWandererTrades(event, mutableListOf(
    // rare coffee seeds
    addTrade(
      TradeRarity.RARE,
      ItemStack(Items.EMERALD, 7),
      ItemStack(WOOD_FAMILY.blocks[BlockFamily.Type.SAPLING]!!.asItem(), 1),
      6,
      2,
      0.2f
    ),
  ))
}