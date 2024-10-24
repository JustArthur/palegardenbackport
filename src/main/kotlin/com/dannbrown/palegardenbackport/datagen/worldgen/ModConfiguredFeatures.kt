package com.dannbrown.palegardenbackport.datagen.worldgen

import com.dannbrown.palegardenbackport.ModContent
import com.dannbrown.palegardenbackport.content.placerTypes.PaleOakFoliagePlacer
import com.dannbrown.palegardenbackport.content.placerTypes.PaleOakTrunkPlacer
import com.dannbrown.palegardenbackport.content.treeDecorator.PaleOakGroundDecorator
import com.dannbrown.palegardenbackport.content.treeDecorator.PaleOakVineDecorator
import com.dannbrown.deltaboxlib.registry.generators.BlockFamily
import com.dannbrown.deltaboxlib.registry.worldgen.AbstractConfiguredFeaturesGen
import com.google.common.collect.ImmutableList
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstapContext
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.util.random.SimpleWeightedRandomList
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.MangrovePropaguleBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator
import net.minecraft.world.level.levelgen.placement.CaveSurface
import java.util.*

object ModConfiguredFeatures: AbstractConfiguredFeaturesGen() {
  override val modId: String = ModContent.MOD_ID

  val PALE_MOSS_PATCH_BONEMEAL: ResourceKey<ConfiguredFeature<*, *>> = registerKey("pale_moss_patch_bonemeal")
  val PALE_MOSS_VEGETATION: ResourceKey<ConfiguredFeature<*, *>> = registerKey("pale_moss_vegetation")
  val PALE_OAK_TREE: ResourceKey<ConfiguredFeature<*, *>> = registerKey("pale_oak_tree")

  override fun bootstrap(context: BootstapContext<ConfiguredFeature<*, *>>) {
    val lookup = context.lookup(Registries.CONFIGURED_FEATURE)

    register<SimpleBlockConfiguration, Feature<SimpleBlockConfiguration>>(context,
      PALE_MOSS_VEGETATION,
      Feature.SIMPLE_BLOCK,
      SimpleBlockConfiguration(WeightedStateProvider(SimpleWeightedRandomList.builder<BlockState>()
        .add(ModContent.PALE_MOSS_CARPET_BLOCK.get().defaultBlockState(), 25)
        .add(Blocks.GRASS.defaultBlockState(), 50)
        .add(Blocks.TALL_GRASS.defaultBlockState(), 10)))
    )

    register<VegetationPatchConfiguration, Feature<VegetationPatchConfiguration>>(context,
      PALE_MOSS_PATCH_BONEMEAL,
      Feature.VEGETATION_PATCH,
      VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(ModContent.PALE_MOSS_BLOCK.get()), PlacementUtils.inlinePlaced(lookup.getOrThrow(PALE_MOSS_VEGETATION)), CaveSurface.FLOOR, ConstantInt.of(1), 0.0f, 5, 0.6f, UniformInt.of(1, 2), 0.75f)
    )

    register<TreeConfiguration, Feature<TreeConfiguration>>(
      context, PALE_OAK_TREE, Feature.TREE,
      TreeConfigurationBuilder(
        BlockStateProvider.simple(ModContent.WOOD_FAMILY.blocks[BlockFamily.Type.LOG]!!.get()),
        PaleOakTrunkPlacer(6, 2, 1),
        BlockStateProvider.simple(ModContent.WOOD_FAMILY.blocks[BlockFamily.Type.LEAVES]!!.get()),
        PaleOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
        ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()))
          .ignoreVines()
          .decorators(
            listOf(
              PaleOakGroundDecorator(BlockStateProvider.simple(ModContent.PALE_MOSS_BLOCK.get()), BlockStateProvider.simple(ModContent.PALE_MOSS_CARPET_BLOCK.get())),
              PaleOakVineDecorator(0.14F, 1, 0, BlockStateProvider.simple(ModContent.PALE_HANGING_MOSS_PLANT.get().defaultBlockState()), 2, mutableListOf(Direction.DOWN), BlockStateProvider.simple(ModContent.PALE_HANGING_MOSS.get().defaultBlockState()))
          )
      )
      .build()
    )
  }
}