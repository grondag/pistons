package grondag.pistons;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DoublePistonHandler {
    private World world;
    private BlockPos posFrom;
    private boolean reverse;
    private BlockPos posTo;
    private Direction direction;
    private final List<BlockPos> movedBlocks = Lists.newArrayList();
    private final List<BlockPos> brokenBlocks = Lists.newArrayList();
    private Direction face;

    private DoublePistonHandler() {
    };

    private DoublePistonHandler prepare(World world, BlockPos pos, Direction face, boolean reverse) {
        this.world = world;
        this.posFrom = pos;
        this.face = face;
        this.reverse = reverse;
        if (reverse) {
            this.direction = face;
            this.posTo = pos.offset(face);
        } else {
            this.direction = face.getOpposite();
            this.posTo = pos.offset(face, 2);
        }
        movedBlocks.clear();
        brokenBlocks.clear();
        return this;
    }

    public boolean calculatePush() {
        final List<BlockPos> movedBlocks = this.movedBlocks;
        final List<BlockPos> brokenBlocks = this.brokenBlocks;
        movedBlocks.clear();
        brokenBlocks.clear();
        
        BlockState blockState = world.getBlockState(posTo);
        if (!DoublePistonBlock.isMovable(blockState, world, posTo, direction, false, face)) {
            if (reverse && blockState.getPistonBehavior() == PistonBehavior.DESTROY) {
                brokenBlocks.add(posTo);
                return true;
            } else {
                return false;
            }
        } else if (!tryMove(posTo, direction)) {
            return false;
        } else {
            for (int i = 0; i < movedBlocks.size(); ++i) {
                BlockPos blockPos_1 = movedBlocks.get(i);
                if (this.world.getBlockState(blockPos_1).getBlock() == Blocks.SLIME_BLOCK && !this.method_11538(blockPos_1)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean tryMove(BlockPos pos, Direction face) {
        final List<BlockPos> movedBlocks = this.movedBlocks;
        final List<BlockPos> brokenBlocks = this.brokenBlocks;
        final World world = this.world;
        final Direction direction = this.direction;
        final BlockPos posFrom = this.posFrom;
        
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return true;
        } else if (!DoublePistonBlock.isMovable(blockState, world, pos, direction, false, face)) {
            return true;
        } else if (pos.equals(posFrom)) {
            return true;
        } else if (movedBlocks.contains(pos)) {
            return true;
        } else {
            int index = 1;
            if (index + movedBlocks.size() > 12) {
                return false;
            } else {
                while (block == Blocks.SLIME_BLOCK) {
                    final BlockPos oppositePos = pos.offset(direction.getOpposite(), index);
                    blockState = world.getBlockState(oppositePos);
                    block = blockState.getBlock();
                    if (blockState.isAir()
                            || !DoublePistonBlock.isMovable(blockState, world, oppositePos, direction, false, direction.getOpposite())
                            || oppositePos.equals(posFrom)) {
                        break;
                    }

                    ++index;
                    if (index + movedBlocks.size() > 12) {
                        return false;
                    }
                }

                int int_2 = 0;

                int int_4;
                for (int_4 = index - 1; int_4 >= 0; --int_4) {
                    movedBlocks.add(pos.offset(direction.getOpposite(), int_4));
                    ++int_2;
                }

                int_4 = 1;

                while (true) {
                    BlockPos blockPos_3 = pos.offset(direction, int_4);
                    int int_5 = movedBlocks.indexOf(blockPos_3);
                    if (int_5 > -1) {
                        method_11539(int_2, int_5);

                        for (int int_6 = 0; int_6 <= int_5 + int_2; ++int_6) {
                            BlockPos blockPos_4 = (BlockPos) movedBlocks.get(int_6);
                            if (world.getBlockState(blockPos_4).getBlock() == Blocks.SLIME_BLOCK && !method_11538(blockPos_4)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    blockState = world.getBlockState(blockPos_3);
                    if (blockState.isAir()) {
                        return true;
                    }

                    if (!DoublePistonBlock.isMovable(blockState, world, blockPos_3, direction, true, direction)
                            || blockPos_3.equals(this.posFrom)) {
                        return false;
                    }

                    if (blockState.getPistonBehavior() == PistonBehavior.DESTROY) {
                        brokenBlocks.add(blockPos_3);
                        return true;
                    }

                    if (movedBlocks.size() >= 12) {
                        return false;
                    }

                    movedBlocks.add(blockPos_3);
                    ++int_2;
                    ++int_4;
                }
            }
        }
    }

    private void method_11539(int int_1, int int_2) {
        List<BlockPos> list_1 = Lists.newArrayList();
        List<BlockPos> list_2 = Lists.newArrayList();
        List<BlockPos> list_3 = Lists.newArrayList();
        list_1.addAll(this.movedBlocks.subList(0, int_2));
        list_2.addAll(this.movedBlocks.subList(this.movedBlocks.size() - int_1, this.movedBlocks.size()));
        list_3.addAll(this.movedBlocks.subList(int_2, this.movedBlocks.size() - int_1));
        this.movedBlocks.clear();
        this.movedBlocks.addAll(list_1);
        this.movedBlocks.addAll(list_2);
        this.movedBlocks.addAll(list_3);
    }

    private boolean method_11538(BlockPos blockPos_1) {
        Direction[] var2 = Direction.values();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Direction direction_1 = var2[var4];
            if (direction_1.getAxis() != this.direction.getAxis() && !this.tryMove(blockPos_1.offset(direction_1), direction_1)) {
                return false;
            }
        }

        return true;
    }

    public List<BlockPos> getMovedBlocks() {
        return this.movedBlocks;
    }

    public List<BlockPos> getBrokenBlocks() {
        return this.brokenBlocks;
    }

    private static ThreadLocal<DoublePistonHandler> POOL = ThreadLocal.withInitial(DoublePistonHandler::new);

    public static DoublePistonHandler get(World world, BlockPos pos, Direction face, boolean reverse) {
        return POOL.get().prepare(world, pos, face, reverse);
    }
}
