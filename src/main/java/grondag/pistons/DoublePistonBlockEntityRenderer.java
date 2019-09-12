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
           if(result == null) {
               result = MinecraftClient.getInstance().getBlockRenderManager();
               manager = result;
           }
           return result;
       }

   @Override
    public void render(DoublePistonBlockEntity pistonBlockEntity_1, double double_1, double double_2, double double_3, float float_1, int int_1) {
          BlockPos blockPos_1 = pistonBlockEntity_1.getPos().offset(pistonBlockEntity_1.method_11506().getOpposite());
          BlockState blockState_1 = pistonBlockEntity_1.getPushedBlock();
          if (!blockState_1.isAir() && pistonBlockEntity_1.getProgress(float_1) < 1.0F) {
             Tessellator tessellator_1 = Tessellator.getInstance();
             BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
             this.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
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
             bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR_UV_LMAP);
             bufferBuilder_1.setOffset(double_1 - (double)blockPos_1.getX() + (double)pistonBlockEntity_1.getRenderOffsetX(float_1), double_2 - (double)blockPos_1.getY() + (double)pistonBlockEntity_1.getRenderOffsetY(float_1), double_3 - (double)blockPos_1.getZ() + (double)pistonBlockEntity_1.getRenderOffsetZ(float_1));
             World world_1 = this.getWorld();
             if (blockState_1.getBlock() == PistonBlocks.DOUBLE_PISTON_HEAD && pistonBlockEntity_1.getProgress(float_1) <= 4.0F) {
                blockState_1 = (BlockState)blockState_1.with(DoublePistonHeadBlock.SHORT, true);
                this.method_3575(blockPos_1, blockState_1, bufferBuilder_1, world_1, false);
             } else if (pistonBlockEntity_1.isSource() && !pistonBlockEntity_1.isExtending()) {
                PistonType pistonType_1 = blockState_1.getBlock() == PistonBlocks.STICKY_DOUBLE_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState blockState_2 = (BlockState)((BlockState)PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.TYPE, pistonType_1)).with(DoublePistonHeadBlock.FACING, blockState_1.get(DoublePistonBlock.FACING));
                blockState_2 = (BlockState)blockState_2.with(DoublePistonHeadBlock.SHORT, pistonBlockEntity_1.getProgress(float_1) >= 0.5F);
                this.method_3575(blockPos_1, blockState_2, bufferBuilder_1, world_1, false);
                BlockPos blockPos_2 = blockPos_1.offset(pistonBlockEntity_1.method_11506());
                bufferBuilder_1.setOffset(double_1 - (double)blockPos_2.getX(), double_2 - (double)blockPos_2.getY(), double_3 - (double)blockPos_2.getZ());
                blockState_1 = (BlockState)blockState_1.with(DoublePistonBlock.EXTENDED, true);
                this.method_3575(blockPos_2, blockState_1, bufferBuilder_1, world_1, true);
             } else {
                this.method_3575(blockPos_1, blockState_1, bufferBuilder_1, world_1, false);
             }
             
             bufferBuilder_1.setOffset(0.0D, 0.0D, 0.0D);
             tessellator_1.draw();
             BlockModelRenderer.disableBrightnessCache();
             GuiLighting.enable();
          }
       }

       private boolean method_3575(BlockPos blockPos_1, BlockState blockState_1, BufferBuilder bufferBuilder_1, World world_1, boolean boolean_1) {
          final BlockRenderManager manager = blockRenderManager();
           return manager.getModelRenderer().tesselate(world_1, manager.getModel(blockState_1), blockState_1, blockPos_1, bufferBuilder_1, boolean_1, new Random(), blockState_1.getRenderingSeed(blockPos_1));
       }
    }