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
    public static final BooleanProperty EXTENDED = Properties.EXTENDED;
    protected static final VoxelShape EXTENDED_EAST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape EXTENDED_WEST_SHAPE = Block.createCuboidShape(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EXTENDED_SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape EXTENDED_NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EXTENDED_UP_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape EXTENDED_DOWN_SHAPE = Block.createCuboidShape(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private final boolean isSticky;

    public DoublePistonBlock(boolean sticky, Block.Settings settings) {
        super(settings);
        this.setDefaultState(stateFactory.getDefaultState().with(FACING, Direction.NORTH).with(EXTENDED, false));
        this.isSticky = sticky;
    }

    @Override
    public boolean canSuffocate(BlockState blockState, BlockView blockView, BlockPos pos) {
        return !(Boolean) blockState.get(EXTENDED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos pos, EntityContext context) {
        if (blockState.get(EXTENDED)) {
            switch (blockState.get(FACING)) {
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
    public boolean isSimpleFullBlock(BlockState blockState, BlockView blockView, BlockPos pos) {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState blockState, LivingEntity entity, ItemStack stack) {
        if (!world.isClient) {
            this.tryMove(world, pos, blockState);
        }

    }

    @Override
    public void neighborUpdate(BlockState blockState, World world, BlockPos pos, Block otherBlock, BlockPos otherPos, boolean flag) {
        if (!world.isClient) {
            this.tryMove(world, pos, blockState);
        }

    }

    @Override
    public void onBlockAdded(BlockState blockState, World world, BlockPos pos, BlockState oldState, boolean flag) {
        if (oldState.getBlock() != blockState.getBlock()) {
            if (!world.isClient && world.getBlockEntity(pos) == null) {
                this.tryMove(world, pos, blockState);
            }

        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite()).with(EXTENDED, false);
    }

    private void tryMove(World world, BlockPos pos, BlockState blockState) {
        final Direction face = blockState.get(FACING);
        final boolean shouldExtend = shouldExtend(world, pos, face);
        final boolean isExtended = blockState.get(EXTENDED);

        if (shouldExtend && !isExtended) {
            if (DoublePistonHandler.get(world, pos, face, true).calculatePush()) {
                world.addBlockAction(pos, this, 0, face.getId());
            }
        } else if (!shouldExtend && isExtended) {
            final BlockPos targetPos = pos.offset(face, 2);
            final BlockState targetState = world.getBlockState(targetPos);
            int distance = 1;

            if (targetState.getBlock() == PistonBlocks.MOVING_DOUBLE_PISTON && targetState.get(FACING) == face) {
                BlockEntity be = world.getBlockEntity(targetPos);
                if (be instanceof DoublePistonBlockEntity) {
                    DoublePistonBlockEntity pbe = (DoublePistonBlockEntity) be;
                    if (pbe.isExtending()
                            && (pbe.getProgress(0.0F) < 0.5F || world.getTime() == pbe.getSavedWorldTime() || ((ServerWorld) world).isInsideTick())) {
                        distance = 2;
                    }
                }
            }

            world.addBlockAction(pos, this, distance, face.getId());
        }

    }

    private static final Direction[] FACES = Direction.values();

    private boolean shouldExtend(World world, BlockPos pos, Direction faceIn) {
        for (int i = 0; i < 6; ++i) {
            Direction face = FACES[i];
            if (face != faceIn && world.isEmittingRedstonePower(pos.offset(face), face)) {
                return true;
            }
        }

        if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
            return true;
        } else {
            BlockPos upPos = pos.up();
            for (int i = 0; i < 6; ++i) {
                Direction face = FACES[i];
                if (face != Direction.DOWN && world.isEmittingRedstonePower(upPos.offset(face), face)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean onBlockAction(BlockState blockState, World world, BlockPos pos, int distance, int faceId) {
        final Direction facing = blockState.get(FACING);
        if (!world.isClient) {
            final boolean shouldExtend = this.shouldExtend(world, pos, facing);
            if (shouldExtend && (distance == 1 || distance == 2)) {
                world.setBlockState(pos, (BlockState) blockState.with(EXTENDED, true), 2);
                return false;
            }

            if (!shouldExtend && distance == 0) {
                return false;
            }
        }

        if (distance == 0) {
            if (!this.move(world, pos, facing, true)) {
                return false;
            }

            world.setBlockState(pos, (BlockState) blockState.with(EXTENDED, true), 67);
            world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
        } else if (distance == 1 || distance == 2) {
            BlockEntity be = world.getBlockEntity(pos.offset(facing));
            if (be instanceof DoublePistonBlockEntity) {
                ((DoublePistonBlockEntity) be).finish();
            }

            world.setBlockState(pos, PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(DoublePistonExtensionBlock.FACING, facing)
                    .with(DoublePistonExtensionBlock.TYPE, isSticky ? PistonType.STICKY : PistonType.DEFAULT), 3);

            world.setBlockEntity(pos,
                    DoublePistonExtensionBlock.createBlockEntityPiston(getDefaultState().with(FACING, Direction.byId(faceId & 7)), facing, false, true));

            // TODO: disallow chaining
            if (isSticky) {
                BlockPos targetPos = pos.add(facing.getOffsetX() * 2, facing.getOffsetY() * 2, facing.getOffsetZ() * 2);
                BlockState targetState = world.getBlockState(targetPos);
                Block targetBlock = targetState.getBlock();
                boolean isChained = false;
                if (targetBlock == PistonBlocks.MOVING_DOUBLE_PISTON) {
                    BlockEntity targetBe = world.getBlockEntity(targetPos);
                    if (targetBe instanceof DoublePistonBlockEntity) {
                        DoublePistonBlockEntity targetPbe = (DoublePistonBlockEntity) targetBe;
                        if (targetPbe.getFacing() == facing && targetPbe.isExtending()) {
                            targetPbe.finish();
                            isChained = true;
                        }
                    }
                }

                if (!isChained) {
                    if (distance != 1 || targetState.isAir() || !isMovable(targetState, world, targetPos, facing.getOpposite(), false, facing)
                            || targetState.getPistonBehavior() != PistonBehavior.NORMAL && targetBlock != PistonBlocks.DOUBLE_PISTON
                                    && targetBlock != PistonBlocks.STICKY_DOUBLE_PISTON) {
                        world.clearBlockState(pos.offset(facing), false);
                    } else {
                        this.move(world, pos, facing, false);
                    }
                }
            } else {
                world.clearBlockState(pos.offset(facing), false);
            }

            world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    @SuppressWarnings("incomplete-switch")
    public static boolean isMovable(BlockState blockState, World world, BlockPos pos, Direction towards, boolean allowDestroy, Direction pistonDirection) {
        final Block block = blockState.getBlock();
        if (block == Blocks.OBSIDIAN) {
            return false;

        } else if (!world.getWorldBorder().contains(pos)) {
            return false;

        } else if (pos.getY() < 0 || towards == Direction.DOWN && pos.getY() == 0) {
            return false;

        } else if (pos.getY() <= world.getHeight() - 1 && (towards != Direction.UP || pos.getY() != world.getHeight() - 1)) {

            if (block != PistonBlocks.DOUBLE_PISTON && block != PistonBlocks.STICKY_DOUBLE_PISTON) {
                if (blockState.getHardness(world, pos) == -1.0F) {
                    return false;
                }

                switch (blockState.getPistonBehavior()) {
                case BLOCK:
                    return false;
                case DESTROY:
                    return allowDestroy;
                case PUSH_ONLY:
                    return towards == pistonDirection;
                }
            } else if (blockState.get(EXTENDED)) {
                return false;
            }

            return !block.hasBlockEntity();
        } else {
            return false;
        }
    }

    private boolean move(World world, BlockPos pos, Direction direction, boolean reverse) {
        final BlockPos targetPos = pos.offset(direction);
        if (!reverse && world.getBlockState(targetPos).getBlock() == PistonBlocks.DOUBLE_PISTON_HEAD) {
            world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 20);
        }

        final DoublePistonHandler handler = DoublePistonHandler.get(world, pos, direction, reverse);
        if (!handler.calculatePush()) {
            return false;

        } else {
            final List<BlockPos> movePositions = handler.getMovedBlocks();
            // PERF: stash this in handler to avoid allocation
            final List<BlockState> moveStates = Lists.newArrayList();

            final int limit = movePositions.size();
            for (int i = 0; i < limit; ++i) {
                moveStates.add(world.getBlockState(movePositions.get(i)));
            }

            final List<BlockPos> brokenPositions = handler.getBrokenBlocks();
            int updateCount = movePositions.size() + brokenPositions.size();

            // PERF: stash this in handler to avoid allocation
            final BlockState[] updates = new BlockState[updateCount];
            final Direction updateDirection = reverse ? direction : direction.getOpposite();

            // PERF: stash this in handler to avoid allocation
            Set<BlockPos> moved = Sets.newHashSet(movePositions);

            for (int i = brokenPositions.size() - 1; i >= 0; --i) {
                BlockPos searchPos = brokenPositions.get(i);
                BlockState searchState = world.getBlockState(searchPos);
                BlockEntity be = searchState.getBlock().hasBlockEntity() ? world.getBlockEntity(searchPos) : null;
                dropStacks(searchState, world, searchPos, be);
                world.setBlockState(searchPos, Blocks.AIR.getDefaultState(), 18);
                --updateCount;
                updates[updateCount] = searchState;
            }

            for (int i = movePositions.size() - 1; i >= 0; --i) {
                BlockPos searchPos = movePositions.get(i);
                BlockState searchState = world.getBlockState(searchPos);
                searchPos = searchPos.offset(updateDirection);
                moved.remove(searchPos);
                world.setBlockState(searchPos, PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(FACING, direction), 68);
                world.setBlockEntity(searchPos, DoublePistonExtensionBlock.createBlockEntityPiston(moveStates.get(i), direction, reverse, false));
                --updateCount;
                updates[updateCount] = searchState;
            }

            BlockState pistonState;
            if (reverse) {
                PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                pistonState = PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.FACING, direction).with(DoublePistonHeadBlock.TYPE,
                        pistonType);

                BlockState searchState = PistonBlocks.MOVING_DOUBLE_PISTON.getDefaultState().with(DoublePistonExtensionBlock.FACING, direction)
                        .with(DoublePistonExtensionBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);

                moved.remove(targetPos);
                world.setBlockState(targetPos, searchState, 68);
                world.setBlockEntity(targetPos, DoublePistonExtensionBlock.createBlockEntityPiston(pistonState, direction, true, true));
            }

            Iterator<BlockPos> it = moved.iterator();

            while (it.hasNext()) {
                BlockPos searchPos = it.next();
                world.setBlockState(searchPos, Blocks.AIR.getDefaultState(), 66);
            }

            for (int i = brokenPositions.size() - 1; i >= 0; --i) {
                pistonState = updates[updateCount++];
                BlockPos brokenPos = brokenPositions.get(i);
                pistonState.method_11637(world, brokenPos, 2);
                world.updateNeighborsAlways(brokenPos, pistonState.getBlock());
            }

            for (int i = movePositions.size() - 1; i >= 0; --i) {
                world.updateNeighborsAlways((BlockPos) movePositions.get(i), updates[updateCount++].getBlock());
            }

            if (reverse) {
                world.updateNeighborsAlways(targetPos, PistonBlocks.DOUBLE_PISTON_HEAD);
            }

            return true;
        }
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
        builder.add(FACING, EXTENDED);
    }

    @Override
    public boolean hasSidedTransparency(BlockState blockState) {
        return blockState.get(EXTENDED);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState, BlockView blockView, BlockPos pos, BlockPlacementEnvironment env) {
        return false;
    }
}
