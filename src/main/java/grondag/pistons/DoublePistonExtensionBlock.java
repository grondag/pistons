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

    public DoublePistonExtensionBlock(Block.Settings block$Settings_1) {
        super(block$Settings_1);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateFactory.getDefaultState()).with(FACING, Direction.NORTH)).with(TYPE, PistonType.DEFAULT));
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockView blockView_1) {
        return null;
    }

    public static BlockEntity createBlockEntityPiston(BlockState blockState_1, Direction direction_1, boolean boolean_1, boolean boolean_2) {
        return new DoublePistonBlockEntity(blockState_1, direction_1, boolean_1, boolean_2);
    }

    @Override
    public void onBlockRemoved(BlockState blockState_1, World world_1, BlockPos blockPos_1, BlockState blockState_2, boolean boolean_1) {
        if (blockState_1.getBlock() != blockState_2.getBlock()) {
            BlockEntity blockEntity_1 = world_1.getBlockEntity(blockPos_1);
            if (blockEntity_1 instanceof DoublePistonBlockEntity) {
                ((DoublePistonBlockEntity)blockEntity_1).finish();
            }

        }
    }

    @Override
    public void onBroken(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1) {
        BlockPos blockPos_2 = blockPos_1.offset(((Direction)blockState_1.get(FACING)).getOpposite());
        BlockState blockState_2 = iWorld_1.getBlockState(blockPos_2);
        if (blockState_2.getBlock() instanceof DoublePistonBlock && (Boolean)blockState_2.get(DoublePistonBlock.EXTENDED)) {
            iWorld_1.clearBlockState(blockPos_2, false);
        }

    }

    @Override
    public boolean isOpaque(BlockState blockState_1) {
        return false;
    }

    @Override
    public boolean isSimpleFullBlock(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
        return false;
    }

    @Override
    public boolean canSuffocate(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
        return false;
    }

    @Override
    public boolean activate(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1, Hand hand_1, BlockHitResult blockHitResult_1) {
        if (!world_1.isClient && world_1.getBlockEntity(blockPos_1) == null) {
            world_1.clearBlockState(blockPos_1, false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState blockState_1, LootContext.Builder lootContext$Builder_1) {
        DoublePistonBlockEntity pistonBlockEntity_1 = this.getPistonBlockEntity(lootContext$Builder_1.getWorld(), (BlockPos)lootContext$Builder_1.get(LootContextParameters.POSITION));
        return pistonBlockEntity_1 == null ? Collections.emptyList() : pistonBlockEntity_1.getPushedBlock().getDroppedStacks(lootContext$Builder_1);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
        DoublePistonBlockEntity pistonBlockEntity_1 = this.getPistonBlockEntity(blockView_1, blockPos_1);
        return pistonBlockEntity_1 != null ? pistonBlockEntity_1.getCollisionShape(blockView_1, blockPos_1) : VoxelShapes.empty();
    }

    @Nullable
    private DoublePistonBlockEntity getPistonBlockEntity(BlockView blockView_1, BlockPos blockPos_1) {
        BlockEntity blockEntity_1 = blockView_1.getBlockEntity(blockPos_1);
        return blockEntity_1 instanceof DoublePistonBlockEntity ? (DoublePistonBlockEntity)blockEntity_1 : null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView blockView_1, BlockPos blockPos_1, BlockState blockState_1) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState blockState_1, BlockRotation blockRotation_1) {
        return (BlockState)blockState_1.with(FACING, blockRotation_1.rotate((Direction)blockState_1.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState_1, BlockMirror blockMirror_1) {
        return blockState_1.rotate(blockMirror_1.getRotation((Direction)blockState_1.get(FACING)));
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> stateFactory$Builder_1) {
        stateFactory$Builder_1.add(FACING, TYPE);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, BlockPlacementEnvironment blockPlacementEnvironment_1) {
        return false;
    }

    static {
        FACING = DoublePistonHeadBlock.FACING;
        TYPE = DoublePistonHeadBlock.TYPE;
    }
}
