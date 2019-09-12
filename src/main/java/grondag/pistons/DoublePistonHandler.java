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
    private final World world;
    private final BlockPos posFrom;
    private final boolean field_12247;
    private final BlockPos posTo;
    private final Direction direction;
    private final List<BlockPos> movedBlocks = Lists.newArrayList();
    private final List<BlockPos> brokenBlocks = Lists.newArrayList();
    private final Direction field_12248;

    public DoublePistonHandler(World world_1, BlockPos blockPos_1, Direction direction_1, boolean boolean_1) {
        this.world = world_1;
        this.posFrom = blockPos_1;
        this.field_12248 = direction_1;
        this.field_12247 = boolean_1;
        if (boolean_1) {
            this.direction = direction_1;
            this.posTo = blockPos_1.offset(direction_1);
        } else {
            this.direction = direction_1.getOpposite();
            this.posTo = blockPos_1.offset(direction_1, 2);
        }

    }

    public boolean calculatePush() {
        this.movedBlocks.clear();
        this.brokenBlocks.clear();
        BlockState blockState_1 = this.world.getBlockState(this.posTo);
        if (!DoublePistonBlock.isMovable(blockState_1, this.world, this.posTo, this.direction, false, this.field_12248)) {
            if (this.field_12247 && blockState_1.getPistonBehavior() == PistonBehavior.DESTROY) {
                this.brokenBlocks.add(this.posTo);
                return true;
            } else {
                return false;
            }
        } else if (!this.tryMove(this.posTo, this.direction)) {
            return false;
        } else {
            for(int int_1 = 0; int_1 < this.movedBlocks.size(); ++int_1) {
                BlockPos blockPos_1 = (BlockPos)this.movedBlocks.get(int_1);
                if (this.world.getBlockState(blockPos_1).getBlock() == Blocks.SLIME_BLOCK && !this.method_11538(blockPos_1)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean tryMove(BlockPos blockPos_1, Direction direction_1) {
        BlockState blockState_1 = this.world.getBlockState(blockPos_1);
        Block block_1 = blockState_1.getBlock();
        if (blockState_1.isAir()) {
            return true;
        } else if (!DoublePistonBlock.isMovable(blockState_1, this.world, blockPos_1, this.direction, false, direction_1)) {
            return true;
        } else if (blockPos_1.equals(this.posFrom)) {
            return true;
        } else if (this.movedBlocks.contains(blockPos_1)) {
            return true;
        } else {
            int int_1 = 1;
            if (int_1 + this.movedBlocks.size() > 12) {
                return false;
            } else {
                while(block_1 == Blocks.SLIME_BLOCK) {
                    BlockPos blockPos_2 = blockPos_1.offset(this.direction.getOpposite(), int_1);
                    blockState_1 = this.world.getBlockState(blockPos_2);
                    block_1 = blockState_1.getBlock();
                    if (blockState_1.isAir() || !DoublePistonBlock.isMovable(blockState_1, this.world, blockPos_2, this.direction, false, this.direction.getOpposite()) || blockPos_2.equals(this.posFrom)) {
                        break;
                    }

                    ++int_1;
                    if (int_1 + this.movedBlocks.size() > 12) {
                        return false;
                    }
                }

                int int_2 = 0;

                int int_4;
                for(int_4 = int_1 - 1; int_4 >= 0; --int_4) {
                    this.movedBlocks.add(blockPos_1.offset(this.direction.getOpposite(), int_4));
                    ++int_2;
                }

                int_4 = 1;

                while(true) {
                    BlockPos blockPos_3 = blockPos_1.offset(this.direction, int_4);
                    int int_5 = this.movedBlocks.indexOf(blockPos_3);
                    if (int_5 > -1) {
                        this.method_11539(int_2, int_5);

                        for(int int_6 = 0; int_6 <= int_5 + int_2; ++int_6) {
                            BlockPos blockPos_4 = (BlockPos)this.movedBlocks.get(int_6);
                            if (this.world.getBlockState(blockPos_4).getBlock() == Blocks.SLIME_BLOCK && !this.method_11538(blockPos_4)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    blockState_1 = this.world.getBlockState(blockPos_3);
                    if (blockState_1.isAir()) {
                        return true;
                    }

                    if (!DoublePistonBlock.isMovable(blockState_1, this.world, blockPos_3, this.direction, true, this.direction) || blockPos_3.equals(this.posFrom)) {
                        return false;
                    }

                    if (blockState_1.getPistonBehavior() == PistonBehavior.DESTROY) {
                        this.brokenBlocks.add(blockPos_3);
                        return true;
                    }

                    if (this.movedBlocks.size() >= 12) {
                        return false;
                    }

                    this.movedBlocks.add(blockPos_3);
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

        for(int var4 = 0; var4 < var3; ++var4) {
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
}
