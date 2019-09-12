package grondag.pistons;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.TagHelper;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DoublePistonBlockEntity extends BlockEntity implements Tickable {

    private BlockState pushedBlock;
    private Direction facing;
    private boolean extending;
    private boolean source;
    private static final ThreadLocal<Direction> field_12205 = new ThreadLocal<Direction>() {
        protected Direction method_11516() {
            return null;
        }

        // $FF: synthetic method
        @Override
        protected Direction initialValue() {
            return this.method_11516();
        }
    };
    private float nextProgress;
    private float progress;
    private long savedWorldTime;

    public DoublePistonBlockEntity() {
        super(PistonBlocks.DOUBLE_PISTON_TYPE);
    }

    public DoublePistonBlockEntity(BlockState blockState_1, Direction direction_1, boolean boolean_1, boolean boolean_2) {
        this();
        this.pushedBlock = blockState_1;
        this.facing = direction_1;
        this.extending = boolean_1;
        this.source = boolean_2;
    }

    @Override
    public CompoundTag toInitialChunkDataTag() {
        return this.toTag(new CompoundTag());
    }

    public boolean isExtending() {
        return this.extending;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public boolean isSource() {
        return this.source;
    }

    public float getProgress(float float_1) {
        if (float_1 > 1.0F) {
            float_1 = 1.0F;
        }

        return MathHelper.lerp(float_1, this.progress, this.nextProgress);
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetX(float float_1) {
        return (float)this.facing.getOffsetX() * this.method_11504(this.getProgress(float_1));
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetY(float float_1) {
        return (float)this.facing.getOffsetY() * this.method_11504(this.getProgress(float_1));
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetZ(float float_1) {
        return (float)this.facing.getOffsetZ() * this.method_11504(this.getProgress(float_1));
    }

    private float method_11504(float float_1) {
        return this.extending ? float_1 - 1.0F : 1.0F - float_1;
    }

    private BlockState method_11496() {
        return !this.isExtending() && this.isSource() && this.pushedBlock.getBlock() instanceof DoublePistonBlock ? PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.TYPE, this.pushedBlock.getBlock() == PistonBlocks.STICKY_DOUBLE_PISTON ? PistonType.STICKY : PistonType.DEFAULT).with(DoublePistonBlock.FACING, this.pushedBlock.get(DoublePistonBlock.FACING)) : this.pushedBlock;
    }

    private void method_11503(float float_1) {
        Direction direction_1 = this.method_11506();
        double double_1 = (double)(float_1 - this.nextProgress);
        VoxelShape voxelShape_1 = this.method_11496().getCollisionShape(this.world, this.getPos());
        if (!voxelShape_1.isEmpty()) {
            List<Box> list_1 = voxelShape_1.getBoundingBoxes();
            Box box_1 = this.method_11500(this.method_11509(list_1));
            List<Entity> list_2 = this.world.getEntities((Entity)null, this.method_11502(box_1, direction_1, double_1).union(box_1));
            if (!list_2.isEmpty()) {
                boolean boolean_1 = this.pushedBlock.getBlock() == Blocks.SLIME_BLOCK;

                for(int int_1 = 0; int_1 < list_2.size(); ++int_1) {
                    Entity entity_1 = (Entity)list_2.get(int_1);
                    if (entity_1.getPistonBehavior() != PistonBehavior.IGNORE) {
                        if (boolean_1) {
                            Vec3d vec3d_1 = entity_1.getVelocity();
                            double double_2 = vec3d_1.x;
                            double double_3 = vec3d_1.y;
                            double double_4 = vec3d_1.z;
                            switch(direction_1.getAxis()) {
                            case X:
                                double_2 = (double)direction_1.getOffsetX();
                                break;
                            case Y:
                                double_3 = (double)direction_1.getOffsetY();
                                break;
                            case Z:
                                double_4 = (double)direction_1.getOffsetZ();
                            }

                            entity_1.setVelocity(double_2, double_3, double_4);
                        }

                        double double_5 = 0.0D;

                        for(int int_2 = 0; int_2 < list_1.size(); ++int_2) {
                            Box box_2 = this.method_11502(this.method_11500((Box)list_1.get(int_2)), direction_1, double_1);
                            Box box_3 = entity_1.getBoundingBox();
                            if (box_2.intersects(box_3)) {
                                double_5 = Math.max(double_5, this.method_11497(box_2, direction_1, box_3));
                                if (double_5 >= double_1) {
                                    break;
                                }
                            }
                        }

                        if (double_5 > 0.0D) {
                            double_5 = Math.min(double_5, double_1) + 0.01D;
                            field_12205.set(direction_1);
                            entity_1.move(MovementType.PISTON, new Vec3d(double_5 * (double)direction_1.getOffsetX(), double_5 * (double)direction_1.getOffsetY(), double_5 * (double)direction_1.getOffsetZ()));
                            field_12205.set(null);
                            if (!this.extending && this.source) {
                                this.method_11514(entity_1, direction_1, double_1);
                            }
                        }
                    }
                }

            }
        }
    }

    public Direction method_11506() {
        return this.extending ? this.facing : this.facing.getOpposite();
    }

    private Box method_11509(List<Box> list_1) {
        double double_1 = 0.0D;
        double double_2 = 0.0D;
        double double_3 = 0.0D;
        double double_4 = 1.0D;
        double double_5 = 1.0D;
        double double_6 = 1.0D;

        Box box_1;
        for(Iterator<Box> var14 = list_1.iterator(); var14.hasNext(); double_6 = Math.max(box_1.maxZ, double_6)) {
            box_1 = (Box)var14.next();
            double_1 = Math.min(box_1.minX, double_1);
            double_2 = Math.min(box_1.minY, double_2);
            double_3 = Math.min(box_1.minZ, double_3);
            double_4 = Math.max(box_1.maxX, double_4);
            double_5 = Math.max(box_1.maxY, double_5);
        }

        return new Box(double_1, double_2, double_3, double_4, double_5, double_6);
    }

    private double method_11497(Box box_1, Direction direction_1, Box box_2) {
        switch(direction_1.getAxis()) {
        case X:
            return method_11493(box_1, direction_1, box_2);
        case Y:
        default:
            return method_11510(box_1, direction_1, box_2);
        case Z:
            return method_11505(box_1, direction_1, box_2);
        }
    }

    private Box method_11500(Box box_1) {
        double double_1 = (double)this.method_11504(this.nextProgress);
        return box_1.offset((double)this.pos.getX() + double_1 * (double)this.facing.getOffsetX(), (double)this.pos.getY() + double_1 * (double)this.facing.getOffsetY(), (double)this.pos.getZ() + double_1 * (double)this.facing.getOffsetZ());
    }

    private Box method_11502(Box box_1, Direction direction_1, double double_1) {
        double double_2 = double_1 * (double)direction_1.getDirection().offset();
        double double_3 = Math.min(double_2, 0.0D);
        double double_4 = Math.max(double_2, 0.0D);
        switch(direction_1) {
        case WEST:
            return new Box(box_1.minX + double_3, box_1.minY, box_1.minZ, box_1.minX + double_4, box_1.maxY, box_1.maxZ);
        case EAST:
            return new Box(box_1.maxX + double_3, box_1.minY, box_1.minZ, box_1.maxX + double_4, box_1.maxY, box_1.maxZ);
        case DOWN:
            return new Box(box_1.minX, box_1.minY + double_3, box_1.minZ, box_1.maxX, box_1.minY + double_4, box_1.maxZ);
        case UP:
        default:
            return new Box(box_1.minX, box_1.maxY + double_3, box_1.minZ, box_1.maxX, box_1.maxY + double_4, box_1.maxZ);
        case NORTH:
            return new Box(box_1.minX, box_1.minY, box_1.minZ + double_3, box_1.maxX, box_1.maxY, box_1.minZ + double_4);
        case SOUTH:
            return new Box(box_1.minX, box_1.minY, box_1.maxZ + double_3, box_1.maxX, box_1.maxY, box_1.maxZ + double_4);
        }
    }

    private void method_11514(Entity entity_1, Direction direction_1, double double_1) {
        Box box_1 = entity_1.getBoundingBox();
        Box box_2 = VoxelShapes.fullCube().getBoundingBox().offset(this.pos);
        if (box_1.intersects(box_2)) {
            Direction direction_2 = direction_1.getOpposite();
            double double_2 = this.method_11497(box_2, direction_2, box_1) + 0.01D;
            double double_3 = this.method_11497(box_2, direction_2, box_1.intersection(box_2)) + 0.01D;
            if (Math.abs(double_2 - double_3) < 0.01D) {
                double_2 = Math.min(double_2, double_1) + 0.01D;
                field_12205.set(direction_1);
                entity_1.move(MovementType.PISTON, new Vec3d(double_2 * (double)direction_2.getOffsetX(), double_2 * (double)direction_2.getOffsetY(), double_2 * (double)direction_2.getOffsetZ()));
                field_12205.set(null);
            }
        }

    }

    private static double method_11493(Box box_1, Direction direction_1, Box box_2) {
        return direction_1.getDirection() == Direction.AxisDirection.POSITIVE ? box_1.maxX - box_2.minX : box_2.maxX - box_1.minX;
    }

    private static double method_11510(Box box_1, Direction direction_1, Box box_2) {
        return direction_1.getDirection() == Direction.AxisDirection.POSITIVE ? box_1.maxY - box_2.minY : box_2.maxY - box_1.minY;
    }

    private static double method_11505(Box box_1, Direction direction_1, Box box_2) {
        return direction_1.getDirection() == Direction.AxisDirection.POSITIVE ? box_1.maxZ - box_2.minZ : box_2.maxZ - box_1.minZ;
    }

    public BlockState getPushedBlock() {
        return this.pushedBlock;
    }

    public void finish() {
        if (this.progress < 1.0F && this.world != null) {
            this.nextProgress = 1.0F;
            this.progress = this.nextProgress;
            this.world.removeBlockEntity(this.pos);
            this.invalidate();
            if (this.world.getBlockState(this.pos).getBlock() == PistonBlocks.MOVING_DOUBLE_PISTON) {
                BlockState blockState_2;
                if (this.source) {
                    blockState_2 = Blocks.AIR.getDefaultState();
                } else {
                    blockState_2 = Block.getRenderingState(this.pushedBlock, this.world, this.pos);
                }

                this.world.setBlockState(this.pos, blockState_2, 3);
                this.world.updateNeighbor(this.pos, blockState_2.getBlock(), this.pos);
            }
        }

    }

    @Override
    public void tick() {
        this.savedWorldTime = this.world.getTime();
        this.progress = this.nextProgress;
        if (this.progress >= 1.0F) {
            this.world.removeBlockEntity(this.pos);
            this.invalidate();
            if (this.pushedBlock != null && this.world.getBlockState(this.pos).getBlock() == PistonBlocks.MOVING_DOUBLE_PISTON) {
                BlockState blockState_1 = Block.getRenderingState(this.pushedBlock, this.world, this.pos);
                if (blockState_1.isAir()) {
                    this.world.setBlockState(this.pos, this.pushedBlock, 84);
                    Block.replaceBlock(this.pushedBlock, blockState_1, this.world, this.pos, 3);
                } else {
                    if (blockState_1.contains(Properties.WATERLOGGED) && (Boolean)blockState_1.get(Properties.WATERLOGGED)) {
                        blockState_1 = (BlockState)blockState_1.with(Properties.WATERLOGGED, false);
                    }

                    this.world.setBlockState(this.pos, blockState_1, 67);
                    this.world.updateNeighbor(this.pos, blockState_1.getBlock(), this.pos);
                }
            }

        } else {
            float float_1 = this.nextProgress + 0.5F;
            this.method_11503(float_1);
            this.nextProgress = float_1;
            if (this.nextProgress >= 1.0F) {
                this.nextProgress = 1.0F;
            }

        }
    }

    @Override
    public void fromTag(CompoundTag compoundTag_1) {
        super.fromTag(compoundTag_1);
        this.pushedBlock = TagHelper.deserializeBlockState(compoundTag_1.getCompound("blockState"));
        this.facing = Direction.byId(compoundTag_1.getInt("facing"));
        this.nextProgress = compoundTag_1.getFloat("progress");
        this.progress = this.nextProgress;
        this.extending = compoundTag_1.getBoolean("extending");
        this.source = compoundTag_1.getBoolean("source");
    }

    @Override
    public CompoundTag toTag(CompoundTag compoundTag_1) {
        super.toTag(compoundTag_1);
        compoundTag_1.put("blockState", TagHelper.serializeBlockState(this.pushedBlock));
        compoundTag_1.putInt("facing", this.facing.getId());
        compoundTag_1.putFloat("progress", this.progress);
        compoundTag_1.putBoolean("extending", this.extending);
        compoundTag_1.putBoolean("source", this.source);
        return compoundTag_1;
    }

    public VoxelShape getCollisionShape(BlockView blockView_1, BlockPos blockPos_1) {
        VoxelShape voxelShape_2;
        if (!this.extending && this.source) {
            voxelShape_2 = ((BlockState)this.pushedBlock.with(DoublePistonBlock.EXTENDED, true)).getCollisionShape(blockView_1, blockPos_1);
        } else {
            voxelShape_2 = VoxelShapes.empty();
        }

        Direction direction_1 = (Direction)field_12205.get();
        if ((double)this.nextProgress < 1.0D && direction_1 == this.method_11506()) {
            return voxelShape_2;
        } else {
            BlockState blockState_2;
            if (this.isSource()) {
                blockState_2 = (PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.FACING, this.facing)).with(DoublePistonHeadBlock.SHORT, this.extending != 1.0F - this.nextProgress < 4.0F);
            } else {
                blockState_2 = this.pushedBlock;
            }

            float float_1 = this.method_11504(this.nextProgress);
            double double_1 = (double)((float)this.facing.getOffsetX() * float_1);
            double double_2 = (double)((float)this.facing.getOffsetY() * float_1);
            double double_3 = (double)((float)this.facing.getOffsetZ() * float_1);
            return VoxelShapes.union(voxelShape_2, blockState_2.getCollisionShape(blockView_1, blockPos_1).offset(double_1, double_2, double_3));
        }
    }

    public long getSavedWorldTime() {
        return this.savedWorldTime;
    }
}
