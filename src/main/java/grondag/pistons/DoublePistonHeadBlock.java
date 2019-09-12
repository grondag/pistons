package grondag.pistons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;

public class DoublePistonHeadBlock extends FacingBlock {
    public static final EnumProperty<PistonType> TYPE;
    public static final BooleanProperty SHORT;
    protected static final VoxelShape EAST_HEAD_SHAPE;
    protected static final VoxelShape WEST_HEAD_SHAPE;
    protected static final VoxelShape SOUTH_HEAD_SHAPE;
    protected static final VoxelShape NORTH_HEAD_SHAPE;
    protected static final VoxelShape UP_HEAD_SHAPE;
    protected static final VoxelShape DOWN_HEAD_SHAPE;
    protected static final VoxelShape UP_ARM_SHAPE;
    protected static final VoxelShape DOWN_ARM_SHAPE;
    protected static final VoxelShape SOUTH_ARM_SHAPE;
    protected static final VoxelShape NORTH_ARM_SHAPE;
    protected static final VoxelShape EAST_ARM_SHAPE;
    protected static final VoxelShape WEST_ARM_SHAPE;
    protected static final VoxelShape SHORT_UP_ARM_SHAPE;
    protected static final VoxelShape SHORT_DOWN_ARM_SHAPE;
    protected static final VoxelShape SHORT_SOUTH_ARM_SHAPE;
    protected static final VoxelShape SHORT_NORTH_ARM_SHAPE;
    protected static final VoxelShape SHORT_EAST_ARM_SHAPE;
    protected static final VoxelShape SHORT_WEST_ARM_SHAPE;

    public DoublePistonHeadBlock(Block.Settings block$Settings_1) {
        super(block$Settings_1);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateFactory.getDefaultState()).with(FACING, Direction.NORTH)).with(TYPE, PistonType.DEFAULT)).with(SHORT, false));
    }

    private VoxelShape getHeadShape(BlockState blockState_1) {
        switch((Direction)blockState_1.get(FACING)) {
        case DOWN:
        default:
            return DOWN_HEAD_SHAPE;
        case UP:
            return UP_HEAD_SHAPE;
        case NORTH:
            return NORTH_HEAD_SHAPE;
        case SOUTH:
            return SOUTH_HEAD_SHAPE;
        case WEST:
            return WEST_HEAD_SHAPE;
        case EAST:
            return EAST_HEAD_SHAPE;
        }
    }

    @Override
    public boolean hasSidedTransparency(BlockState blockState_1) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
        return VoxelShapes.union(this.getHeadShape(blockState_1), this.getArmShape(blockState_1));
    }

    private VoxelShape getArmShape(BlockState blockState_1) {
        boolean boolean_1 = (Boolean)blockState_1.get(SHORT);
        switch((Direction)blockState_1.get(FACING)) {
        case DOWN:
        default:
            return boolean_1 ? SHORT_DOWN_ARM_SHAPE : DOWN_ARM_SHAPE;
        case UP:
            return boolean_1 ? SHORT_UP_ARM_SHAPE : UP_ARM_SHAPE;
        case NORTH:
            return boolean_1 ? SHORT_NORTH_ARM_SHAPE : NORTH_ARM_SHAPE;
        case SOUTH:
            return boolean_1 ? SHORT_SOUTH_ARM_SHAPE : SOUTH_ARM_SHAPE;
        case WEST:
            return boolean_1 ? SHORT_WEST_ARM_SHAPE : WEST_ARM_SHAPE;
        case EAST:
            return boolean_1 ? SHORT_EAST_ARM_SHAPE : EAST_ARM_SHAPE;
        }
    }

    @Override
    public void onBreak(World world_1, BlockPos blockPos_1, BlockState blockState_1, PlayerEntity playerEntity_1) {
        if (!world_1.isClient && playerEntity_1.abilities.creativeMode) {
            BlockPos blockPos_2 = blockPos_1.offset(((Direction)blockState_1.get(FACING)).getOpposite());
            Block block_1 = world_1.getBlockState(blockPos_2).getBlock();
            if (block_1 == PistonBlocks.DOUBLE_PISTON || block_1 == PistonBlocks.STICKY_DOUBLE_PISTON) {
                world_1.clearBlockState(blockPos_2, false);
            }
        }

        super.onBreak(world_1, blockPos_1, blockState_1, playerEntity_1);
    }

    @Override
    public void onBlockRemoved(BlockState blockState_1, World world_1, BlockPos blockPos_1, BlockState blockState_2, boolean boolean_1) {
        if (blockState_1.getBlock() != blockState_2.getBlock()) {
            super.onBlockRemoved(blockState_1, world_1, blockPos_1, blockState_2, boolean_1);
            Direction direction_1 = ((Direction)blockState_1.get(FACING)).getOpposite();
            blockPos_1 = blockPos_1.offset(direction_1);
            BlockState blockState_3 = world_1.getBlockState(blockPos_1);
            if ((blockState_3.getBlock() == PistonBlocks.DOUBLE_PISTON || blockState_3.getBlock() == PistonBlocks.STICKY_DOUBLE_PISTON) && (Boolean)blockState_3.get(DoublePistonBlock.EXTENDED)) {
                dropStacks(blockState_3, world_1, blockPos_1);
                world_1.clearBlockState(blockPos_1, false);
            }

        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, IWorld iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
        return direction_1.getOpposite() == blockState_1.get(FACING) && !blockState_1.canPlaceAt(iWorld_1, blockPos_1) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
    }

    @Override
    public boolean canPlaceAt(BlockState blockState_1, ViewableWorld viewableWorld_1, BlockPos blockPos_1) {
        Block block_1 = viewableWorld_1.getBlockState(blockPos_1.offset(((Direction)blockState_1.get(FACING)).getOpposite())).getBlock();
        return block_1 == PistonBlocks.DOUBLE_PISTON || block_1 == PistonBlocks.STICKY_DOUBLE_PISTON || block_1 == PistonBlocks.MOVING_DOUBLE_PISTON;
    }

    @Override
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1) {
        if (blockState_1.canPlaceAt(world_1, blockPos_1)) {
            BlockPos blockPos_3 = blockPos_1.offset(((Direction)blockState_1.get(FACING)).getOpposite());
            world_1.getBlockState(blockPos_3).neighborUpdate(world_1, blockPos_3, block_1, blockPos_2, false);
        }

    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView blockView_1, BlockPos blockPos_1, BlockState blockState_1) {
        return new ItemStack(blockState_1.get(TYPE) == PistonType.STICKY ? PistonBlocks.STICKY_DOUBLE_PISTON : PistonBlocks.DOUBLE_PISTON);
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
        stateFactory$Builder_1.add(FACING, TYPE, SHORT);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, BlockPlacementEnvironment blockPlacementEnvironment_1) {
        return false;
    }

    static {
        TYPE = Properties.PISTON_TYPE;
        SHORT = Properties.SHORT;
        EAST_HEAD_SHAPE = Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        WEST_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
        SOUTH_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
        NORTH_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
        UP_HEAD_SHAPE = Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        DOWN_HEAD_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
        UP_ARM_SHAPE = Block.createCuboidShape(6.0D, -4.0D, 6.0D, 10.0D, 12.0D, 10.0D);
        DOWN_ARM_SHAPE = Block.createCuboidShape(6.0D, 4.0D, 6.0D, 10.0D, 20.0D, 10.0D);
        SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, -4.0D, 10.0D, 10.0D, 12.0D);
        NORTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 20.0D);
        EAST_ARM_SHAPE = Block.createCuboidShape(-4.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
        WEST_ARM_SHAPE = Block.createCuboidShape(4.0D, 6.0D, 6.0D, 20.0D, 10.0D, 10.0D);
        SHORT_UP_ARM_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
        SHORT_DOWN_ARM_SHAPE = Block.createCuboidShape(6.0D, 4.0D, 6.0D, 10.0D, 16.0D, 10.0D);
        SHORT_SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 12.0D);
        SHORT_NORTH_ARM_SHAPE = Block.createCuboidShape(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 16.0D);
        SHORT_EAST_ARM_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
        SHORT_WEST_ARM_SHAPE = Block.createCuboidShape(4.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    }
}
