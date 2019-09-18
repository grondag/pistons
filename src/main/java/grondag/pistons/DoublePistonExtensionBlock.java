package grondag.pistons;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;
import net.minecraft.world.loot.context.LootContextParameters;

public class DoublePistonExtensionBlock extends BlockWithEntity {
    public static final DirectionProperty FACING;
    public static final EnumProperty<PistonType> TYPE;

    public DoublePistonExtensionBlock(Block.Settings settings) {
        super(settings);
        this.setDefaultState(stateFactory.getDefaultState().with(FACING, Direction.NORTH).with(TYPE, PistonType.DEFAULT));
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockView blockView) {
        return null;
    }

    public static BlockEntity createBlockEntityPiston(BlockState blockState, Direction face, boolean isExtending, boolean isSource) {
        return new DoublePistonBlockEntity(blockState, face, isExtending, isSource);
    }

    @Override
    public void onBlockRemoved(BlockState blockState, World world, BlockPos pos, BlockState removedState, boolean flag) {
        if (blockState.getBlock() != removedState.getBlock()) {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof DoublePistonBlockEntity) {
                ((DoublePistonBlockEntity) blockEntity).finish();
            }
        }
    }

    @Override
    public void onBroken(IWorld world, BlockPos pos, BlockState blockState) {
        final BlockPos oppositePos = pos.offset((blockState.get(FACING)).getOpposite());
        final BlockState oppositeState = world.getBlockState(oppositePos);
        if (oppositeState.getBlock() instanceof DoublePistonBlock && oppositeState.get(DoublePistonBlock.EXTENDED)) {
            world.clearBlockState(oppositePos, false);
        }
    }

    @Override
    public boolean isOpaque(BlockState blockState) {
        return false;
    }

    @Override
    public boolean isSimpleFullBlock(BlockState blockState, BlockView blockView, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canSuffocate(BlockState blockState, BlockView blockView, BlockPos pos) {
        return false;
    }

    @Override
    public boolean activate(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) == null) {
            world.clearBlockState(pos, false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState blockState, LootContext.Builder builder) {
        final DoublePistonBlockEntity blockEntity = getPistonBlockEntity(builder.getWorld(), builder.get(LootContextParameters.POSITION));
        return blockEntity == null ? Collections.emptyList() : blockEntity.getPushedBlock().getDroppedStacks(builder);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos pos, EntityContext ctx) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockView blockView, BlockPos pos, EntityContext ctx) {
        final DoublePistonBlockEntity blockEntity = getPistonBlockEntity(blockView, pos);
        return blockEntity != null ? blockEntity.getCollisionShape(blockView, pos) : VoxelShapes.empty();
    }

    @Nullable
    private DoublePistonBlockEntity getPistonBlockEntity(BlockView blockView, BlockPos pos) {
        final BlockEntity blockEntity = blockView.getBlockEntity(pos);
        return blockEntity instanceof DoublePistonBlockEntity ? (DoublePistonBlockEntity) blockEntity : null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView blockView, BlockPos pos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState blockState, BlockRotation rotation) {
        return blockState.with(FACING, rotation.rotate(blockState.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, BlockMirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.get(FACING)));
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState, BlockView blockView, BlockPos pos, BlockPlacementEnvironment env) {
        return false;
    }

    static {
        FACING = DoublePistonHeadBlock.FACING;
        TYPE = DoublePistonHeadBlock.TYPE;
    }
}
