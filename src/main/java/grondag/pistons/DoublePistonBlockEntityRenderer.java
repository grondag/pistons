package grondag.pistons;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class DoublePistonBlockEntityRenderer extends BlockEntityRenderer<DoublePistonBlockEntity> {
    private static BlockRenderManager manager;

    private static BlockRenderManager blockRenderManager() {
        BlockRenderManager result = manager;
        if (result == null) {
            result = MinecraftClient.getInstance().getBlockRenderManager();
            manager = result;
        }
        return result;
    }

    @Override
    public void render(DoublePistonBlockEntity blockEntity, double x, double y, double z, float tickDelta, int int_1) {
        final BlockPos pos = blockEntity.getPos().offset(blockEntity.moveDirection().getOpposite());
        BlockState pushedState = blockEntity.getPushedBlock();
        if (!pushedState.isAir() && blockEntity.getProgress(tickDelta) < 1.0F) {
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferBuilder = tessellator.getBufferBuilder();
            bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            GuiLighting.disable();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            if (MinecraftClient.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(7425);
            } else {
                GlStateManager.shadeModel(7424);
            }

            BlockModelRenderer.enableBrightnessCache();
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_UV_LMAP);
            bufferBuilder.setOffset(x - pos.getX() + blockEntity.getRenderOffsetX(tickDelta), y - pos.getY() + blockEntity.getRenderOffsetY(tickDelta),
                    z - pos.getZ() + blockEntity.getRenderOffsetZ(tickDelta));

            final World world = this.getWorld();
            if (pushedState.getBlock() == PistonBlocks.DOUBLE_PISTON_HEAD && blockEntity.getProgress(tickDelta) <= 4.0F) {
                pushedState = pushedState.with(DoublePistonHeadBlock.SHORT, true);
                renderPushed(pos, pushedState, bufferBuilder, world, false);
            } else if (blockEntity.isSource() && !blockEntity.isExtending()) {
                PistonType pistonType = pushedState.getBlock() == PistonBlocks.STICKY_DOUBLE_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState renderstate = PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.TYPE, pistonType)
                        .with(DoublePistonHeadBlock.FACING, pushedState.get(DoublePistonBlock.FACING));
                renderstate = renderstate.with(DoublePistonHeadBlock.SHORT, blockEntity.getProgress(tickDelta) >= 0.5F);
                renderPushed(pos, renderstate, bufferBuilder, world, false);

                BlockPos pushPos = pos.offset(blockEntity.moveDirection());
                bufferBuilder.setOffset(x - pushPos.getX(), y - pushPos.getY(), z - pushPos.getZ());
                pushedState = pushedState.with(DoublePistonBlock.EXTENDED, true);
                renderPushed(pushPos, pushedState, bufferBuilder, world, true);
            } else {
                this.renderPushed(pos, pushedState, bufferBuilder, world, false);
            }

            bufferBuilder.setOffset(0.0D, 0.0D, 0.0D);
            tessellator.draw();
            BlockModelRenderer.disableBrightnessCache();
            GuiLighting.enable();
        }
    }

    private boolean renderPushed(BlockPos pos, BlockState blockState, BufferBuilder bufferBuilder, World world, boolean cullFlag) {
        final BlockRenderManager manager = blockRenderManager();
        return manager.getModelRenderer().tesselate(world, manager.getModel(blockState), blockState, pos, bufferBuilder, cullFlag, new Random(),
                blockState.getRenderingSeed(pos));
    }
}