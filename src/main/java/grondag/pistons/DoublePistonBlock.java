package grondag.pistons;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DoublePistonBlock extends FacingBlock {
    public static final BooleanProperty EXTENDED;
    protected static final VoxelShape EXTENDED_EAST_SHAPE;
    protected static final VoxelShape EXTENDED_WEST_SHAPE;
    protected static final VoxelShape EXTENDED_SOUTH_SHAPE;
    protected static final VoxelShape EXTENDED_NORTH_SHAPE;
    protected static final VoxelShape EXTENDED_UP_SHAPE;
    protected static final VoxelShape EXTENDED_DOWN_SHAPE;
    private final boolean isSticky;

    public DoublePistonBlock(boolean boolean_1, Block.Settings block$Settings_1) {
        super(block$Settings_1);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateFactory.getDefaultState()).with(FACING, Direction.NORTH)).with(EXTENDED, false));
        this.isSticky = boolean_1;
    }

    @Override
    public boolean canSuffocate(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
        return !(Boolean)blockState_1.get(EXTENDED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
        if ((Boolean)blockState_1.get(EXTENDED)) {
            switch((Direction)blockState_1.get(FACING)) {
            case DOWN:
                return EXTENDED_DOWN_SHAPE;
            case UP:
            default:
                return EXTENDED_UP_SHAPE;
            case NORTH:
                return EXTENDED_NORTH_SHAPE;
            case SOUTH:
                return EXTENDED_SOUTH_SHAPE;
            case WEST:
                return EXTENDED_WEST_SHAPE;
            case EAST:
                return EXTENDED_EAST_SHAPE;
            }
        } else {
            return VoxelShapes.fullCube();
        }
    }

    @Override
    public boolean isSimpleFullBlock(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1) {
        return false;
    }

    @Override
    public void onPlaced(World world_1, BlockPos blockPos_1, BlockState blockState_1, LivingEntity livingEntity_1, ItemStack itemStack_1) {
        if (!world_1.isClient) {
            this.tryMove(world_1, blockPos_1, blockState_1);
        }

    }

    @Override
    public void neighborUpdate(BlockState blockState_1, World world_1, BlockPos blockPos_1, Block block_1, BlockPos blockPos_2, boolean boolean_1) {
        if (!world_1.isClient) {
            this.tryMove(world_1, blockPos_1, blockState_1);
        }

    }

    @Override
    public void onBlockAdded(BlockState blockState_1, World world_1, BlockPos blockPos_1, BlockState blockState_2, boolean boolean_1) {
        if (blockState_2.getBlock() != blockState_1.getBlock()) {
            if (!world_1.isClient && world_1.getBlockEntity(blockPos_1) == null) {
                this.tryMove(world_1, blockPos_1, blockState_1);
            }

        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext_1) {
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, itemPlacementContext_1.getPlayerLookDirection().getOpposite())).with(EXTENDED, false);
    }

    private void tryMove(World world_1, BlockPos blockPos_1, BlockState blockState_1) {
        Direction direction_1 = (Direction)blockState_1.get(FACING);
        boolean boolean_1 = this.shouldExtend(world_1, blockPos_1, direction_1);
        if (boolean_1 && !(Boolean)blockState_1.get(EXTENDED)) {
            if ((new DoublePistonHandler(world_1, blockPos_1, direction_1, true)).calculatePush()) {
                world_1.addBlockAction(blockPos_1, this, 0, direction_1.getId());
            }
        } else if (!boolean_1 && (Boolean)blockState_1.get(EXTENDED)) {
            BlockPos blockPos_2 = blockPos_1.offset(direction_1, 2);
            BlockState blockState_2 = world_1.getBlockState(blockPos_2);
            int int_1 = 1;
            if (blockState_2.getBlock() == PistonBlocks.MOVING_DOUBLE_PISTON && blockState_2.get(FACING) == direction_1) {
                BlockEntity blockEntity_1 = world_1.getBlockEntity(blockPos_2);
                if (blockEntity_1 instanceof DoublePistonBlockEntity) {
                    DoublePistonBlockEntity pistonBlockEntity_1 = (DoublePistonBlockEntity)blockEntity_1;
                    if (pistonBlockEntity_1.isExtending() && (pistonBlockEntity_1.getProgress(0.0F) < 0.5F || world_1.getTime() == pistonBlockEntity_1.getSavedWorldTime() || ((ServerWorld)world_1).isInsideTick())) {
                        int_1 = 2;
                    }
                }
            }

            world_1.addBlockAction(blockPos_1, this, int_1, direction_1.getId());
        }

    }

    private boolean shouldExtend(World world_1, BlockPos blockPos_1, Direction direction_1) {
        Direction[] var4 = Direction.values();
        int var5 = var4.length;

        int var6;
        for(var6 = 0; var6 < var5; ++var6) {
            Direction direction_2 = var4[var6];
            if (direction_2 != direction_1 && world_1.isEmittingRedstonePower(blockPos_1.offset(direction_2), direction_2)) {
                return true;
            }
        }

        if (world_1.isEmittingRedstonePower(blockPos_1, Direction.DOWN)) {
            return true;
        } else {
            BlockPos blockPos_2 = blockPos_1.up();
            Direction[] var10 = Direction.values();
            var6 = var10.length;

            for(int var11 = 0; var11 < var6; ++var11) {
                Direction direction_3 = var10[var11];
                if (direction_3 != Direction.DOWN && world_1.isEmittingRedstonePower(blockPos_2.offset(direction_3), direction_3)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean onBlockAction(BlockState blockState_1, World world_1, BlockPos blockPos_1, int int_1, int int_2) {
        Direction direction_1 = (Direction)blockState_1.get(FACING);
        if (!world_1.isClient) {
            boolean boolean_1 = this.shouldExtend(world_1, blockPos_1, direction_1);
            if (boolean_1 && (int_1 == 1 || int_1 == 2)) {
                world_1.setBlockState(blockPos_1, (BlockState)blockState_1.with(EXTENDED, true), 2);
                return false;
            }

            if (!boolean_1 && int_1 == 0) {
                return false;
            }
        }

        if (int_1 == 0) {
            if (!this.move(world_1, blockPos_1, direction_1, true)) {
                return false;
            }

            world_1.setBlockState(blockPos_1, (BlockState)blockState_1.with(EXTENDED, true), 67);
            world_1.playSound((PlayerEntity)null, blockPos_1, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world_1.random.nextFloat() * 0.25F + 0.6F);
        } else if (int_1 == 1 || int_1 == 2) {
            BlockEntity blockEntity_1 = world_1.getBlockEntity(blockPos_1.offset(direction_1));
            if (blockEntity_1 instanceof DoublePistonBlockEntity) {
                ((DoublePistonBlockEntity)blockEntity_1).finish();
            }

            world_1.setBlockState(blockPos_1, (BlockState)((BlockState)PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(DoublePistonExtensionBlock.FACING, direction_1)).with(DoublePistonExtensionBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT), 3);
            world_1.setBlockEntity(blockPos_1, DoublePistonExtensionBlock.createBlockEntityPiston((BlockState)this.getDefaultState().with(FACING, Direction.byId(int_2 & 7)), direction_1, false, true));
            if (this.isSticky) {
                BlockPos blockPos_2 = blockPos_1.add(direction_1.getOffsetX() * 2, direction_1.getOffsetY() * 2, direction_1.getOffsetZ() * 2);
                BlockState blockState_2 = world_1.getBlockState(blockPos_2);
                Block block_1 = blockState_2.getBlock();
                boolean boolean_2 = false;
                if (block_1 == PistonBlocks.MOVING_DOUBLE_PISTON) {
                    BlockEntity blockEntity_2 = world_1.getBlockEntity(blockPos_2);
                    if (blockEntity_2 instanceof DoublePistonBlockEntity) {
                        DoublePistonBlockEntity pistonBlockEntity_1 = (DoublePistonBlockEntity)blockEntity_2;
                        if (pistonBlockEntity_1.getFacing() == direction_1 && pistonBlockEntity_1.isExtending()) {
                            pistonBlockEntity_1.finish();
                            boolean_2 = true;
                        }
                    }
                }

                if (!boolean_2) {
                    if (int_1 != 1 || blockState_2.isAir() || !isMovable(blockState_2, world_1, blockPos_2, direction_1.getOpposite(), false, direction_1) || blockState_2.getPistonBehavior() != PistonBehavior.NORMAL && block_1 != PistonBlocks.DOUBLE_PISTON && block_1 != PistonBlocks.STICKY_DOUBLE_PISTON) {
                        world_1.clearBlockState(blockPos_1.offset(direction_1), false);
                    } else {
                        this.move(world_1, blockPos_1, direction_1, false);
                    }
                }
            } else {
                world_1.clearBlockState(blockPos_1.offset(direction_1), false);
            }

            world_1.playSound((PlayerEntity)null, blockPos_1, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world_1.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    @SuppressWarnings("incomplete-switch")
    public static boolean isMovable(BlockState blockState_1, World world_1, BlockPos blockPos_1, Direction direction_1, boolean boolean_1, Direction direction_2) {
        Block block_1 = blockState_1.getBlock();
        if (block_1 == Blocks.OBSIDIAN) {
            return false;
        } else if (!world_1.getWorldBorder().contains(blockPos_1)) {
            return false;
        } else if (blockPos_1.getY() < 0 || direction_1 == Direction.DOWN && blockPos_1.getY() == 0) {
            return false;
        } else if (blockPos_1.getY() <= world_1.getHeight() - 1 && (direction_1 != Direction.UP || blockPos_1.getY() != world_1.getHeight() - 1)) {
            if (block_1 != PistonBlocks.DOUBLE_PISTON && block_1 != PistonBlocks.STICKY_DOUBLE_PISTON) {
                if (blockState_1.getHardness(world_1, blockPos_1) == -1.0F) {
                    return false;
                }

                switch(blockState_1.getPistonBehavior()) {
                case BLOCK:
                    return false;
                case DESTROY:
                    return boolean_1;
                case PUSH_ONLY:
                    return direction_1 == direction_2;
                }
            } else if ((Boolean)blockState_1.get(EXTENDED)) {
                return false;
            }

            return !block_1.hasBlockEntity();
        } else {
            return false;
        }
    }

    private boolean move(World world_1, BlockPos blockPos_1, Direction direction_1, boolean boolean_1) {
        BlockPos blockPos_2 = blockPos_1.offset(direction_1);
        if (!boolean_1 && world_1.getBlockState(blockPos_2).getBlock() == PistonBlocks.DOUBLE_PISTON_HEAD) {
            world_1.setBlockState(blockPos_2, Blocks.AIR.getDefaultState(), 20);
        }

        DoublePistonHandler pistonHandler_1 = new DoublePistonHandler(world_1, blockPos_1, direction_1, boolean_1);
        if (!pistonHandler_1.calculatePush()) {
            return false;
        } else {
            List<BlockPos> list_1 = pistonHandler_1.getMovedBlocks();
            List<BlockState> list_2 = Lists.newArrayList();

            for(int int_1 = 0; int_1 < list_1.size(); ++int_1) {
                BlockPos blockPos_3 = (BlockPos)list_1.get(int_1);
                list_2.add(world_1.getBlockState(blockPos_3));
            }

            List<BlockPos> list_3 = pistonHandler_1.getBrokenBlocks();
            int int_2 = list_1.size() + list_3.size();
            BlockState[] blockStates_1 = new BlockState[int_2];
            Direction direction_2 = boolean_1 ? direction_1 : direction_1.getOpposite();
            Set<BlockPos> set_1 = Sets.newHashSet(list_1);

            int int_6;
            BlockPos blockPos_6;
            BlockState blockState_4;
            for(int_6 = list_3.size() - 1; int_6 >= 0; --int_6) {
                blockPos_6 = (BlockPos)list_3.get(int_6);
                blockState_4 = world_1.getBlockState(blockPos_6);
                BlockEntity blockEntity_1 = blockState_4.getBlock().hasBlockEntity() ? world_1.getBlockEntity(blockPos_6) : null;
                dropStacks(blockState_4, world_1, blockPos_6, blockEntity_1);
                world_1.setBlockState(blockPos_6, Blocks.AIR.getDefaultState(), 18);
                --int_2;
                blockStates_1[int_2] = blockState_4;
            }

            for(int_6 = list_1.size() - 1; int_6 >= 0; --int_6) {
                blockPos_6 = (BlockPos)list_1.get(int_6);
                blockState_4 = world_1.getBlockState(blockPos_6);
                blockPos_6 = blockPos_6.offset(direction_2);
                set_1.remove(blockPos_6);
                world_1.setBlockState(blockPos_6, (BlockState)PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(FACING, direction_1), 68);
                world_1.setBlockEntity(blockPos_6, DoublePistonExtensionBlock.createBlockEntityPiston((BlockState)list_2.get(int_6), direction_1, boolean_1, false));
                --int_2;
                blockStates_1[int_2] = blockState_4;
            }

            BlockState blockState_5;
            if (boolean_1) {
                PistonType pistonType_1 = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                blockState_5 = (BlockState)((BlockState)PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.FACING, direction_1)).with(DoublePistonHeadBlock.TYPE, pistonType_1);
                blockState_4 = (BlockState)((BlockState)PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(DoublePistonExtensionBlock.FACING, direction_1)).with(DoublePistonExtensionBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
                set_1.remove(blockPos_2);
                world_1.setBlockState(blockPos_2, blockState_4, 68);
                world_1.setBlockEntity(blockPos_2, DoublePistonExtensionBlock.createBlockEntityPiston(blockState_5, direction_1, true, true));
            }

            Iterator<BlockPos> var21 = set_1.iterator();

            while(var21.hasNext()) {
                blockPos_6 = (BlockPos)var21.next();
                world_1.setBlockState(blockPos_6, Blocks.AIR.getDefaultState(), 66);
            }

            for(int_6 = list_3.size() - 1; int_6 >= 0; --int_6) {
                blockState_5 = blockStates_1[int_2++];
                BlockPos blockPos_7 = (BlockPos)list_3.get(int_6);
                blockState_5.method_11637(world_1, blockPos_7, 2);
                world_1.updateNeighborsAlways(blockPos_7, blockState_5.getBlock());
            }

            for(int_6 = list_1.size() - 1; int_6 >= 0; --int_6) {
                world_1.updateNeighborsAlways((BlockPos)list_1.get(int_6), blockStates_1[int_2++].getBlock());
            }

            if (boolean_1) {
                world_1.updateNeighborsAlways(blockPos_2, PistonBlocks.DOUBLE_PISTON_HEAD);
            }

            return true;
        }
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
        stateFactory$Builder_1.add(FACING, EXTENDED);
    }

    @Override
    public boolean hasSidedTransparency(BlockState blockState_1) {
        return (Boolean)blockState_1.get(EXTENDED);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, BlockPlacementEnvironment blockPlacementEnvironment_1) {
        return false;
    }

    static {
        EXTENDED = Properties.EXTENDED;
        EXTENDED_EAST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
        EXTENDED_WEST_SHAPE = Block.createCuboidShape(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        EXTENDED_SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
        EXTENDED_NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
        EXTENDED_UP_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
        EXTENDED_DOWN_SHAPE = Block.createCuboidShape(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }
}
