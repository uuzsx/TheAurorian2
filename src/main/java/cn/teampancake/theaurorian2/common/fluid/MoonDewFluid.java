package cn.teampancake.theaurorian2.common.fluid;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModFluidTypes;
import cn.teampancake.theaurorian2.common.registry.ModFluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class MoonDewFluid extends WaterFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_MOON_DEW.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.MOON_DEW.get();
    }

    @Override
    public Item getBucket() {
        return ModBlocks.MOON_DEW_BUCKET.get();
    }

    @Override
    public FluidType getFluidType() {
        return ModFluidTypes.MOON_DEW.get();
    }

    @Override
    public BlockState createLegacyBlock(FluidState fluidState) {
        return ModBlocks.MOON_DEW_BLOCK.get()
                .defaultBlockState()
                .setValue(LiquidBlock.LEVEL, FlowingFluid.getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(Fluid other) {
        return other == ModFluids.MOON_DEW.get() || other == ModFluids.FLOWING_MOON_DEW.get();
    }

    public static final class Flowing extends MoonDewFluid {

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }

    public static final class Source extends MoonDewFluid {

        @Override
        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }
}
