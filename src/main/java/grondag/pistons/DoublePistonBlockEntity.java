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
import net.minecraft.util.math.Direction.AxisDirection;
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
    private static final ThreadLocal<Direction> threadFace = ThreadLocal.withInitial(() -> null);

    private float nextProgress;
    private float progress;
    private long savedWorldTime;

    public DoublePistonBlockEntity() {
        super(PistonBlocks.DOUBLE_PISTON_TYPE);
    }

    public DoublePistonBlockEntity(BlockState blockState, Direction facing, boolean isExtending, boolean isSource) {
        this();
        this.pushedBlock = blockState;
        this.facing = facing;
        this.extending = isExtending;
        this.source = isSource;
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

    public float getProgress(float tickDelta) {
        if (tickDelta > 1.0F) {
            tickDelta = 1.0F;
        }

        return MathHelper.lerp(tickDelta, this.progress, this.nextProgress);
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetX(float tickDelta) {
        return facing.getOffsetX() * normalize(getProgress(tickDelta));
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetY(float tickDelta) {
        return facing.getOffsetY() * normalize(getProgress(tickDelta));
    }

    @Environment(EnvType.CLIENT)
    public float getRenderOffsetZ(float tickDelta) {
        return facing.getOffsetZ() * normalize(getProgress(tickDelta));
    }

    private float normalize(float tickDelta) {
        return extending ? tickDelta - 1.0F : 1.0F - tickDelta;
    }

    private BlockState collisionState() {
        return !this.isExtending() && this.isSource() && this.pushedBlock.getBlock() instanceof DoublePistonBlock ? PistonBlocks.DOUBLE_PISTON_HEAD
                .getDefaultState()
                .with(DoublePistonHeadBlock.TYPE, this.pushedBlock.getBlock() == PistonBlocks.STICKY_DOUBLE_PISTON ? PistonType.STICKY : PistonType.DEFAULT)
                .with(DoublePistonBlock.FACING, this.pushedBlock.get(DoublePistonBlock.FACING)) : this.pushedBlock;
    }

    private void tickInner(float progress) {
        final Direction moveDirection = this.moveDirection();
        final double progressDelta = progress - this.nextProgress;
        final VoxelShape shape = this.collisionState().getCollisionShape(world, getPos());

        if (!shape.isEmpty()) {
            final List<Box> boxes = shape.getBoundingBoxes();
            final Box box = this.expandToProgress(union(boxes));

            final List<Entity> entities = world.getEntities((Entity) null, expandIncrement(box, moveDirection, progressDelta).union(box));
            if (!entities.isEmpty()) {
                final boolean isSlime = this.pushedBlock.getBlock() == Blocks.SLIME_BLOCK;

                for (int i = 0; i < entities.size(); ++i) {
                    Entity entity = entities.get(i);
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        if (isSlime) {
                            Vec3d velocity = entity.getVelocity();
                            double x = velocity.x;
                            double y = velocity.y;
                            double z = velocity.z;
                            switch (moveDirection.getAxis()) {
                            case X:
                                x = moveDirection.getOffsetX();
                                break;
                            case Y:
                                y = moveDirection.getOffsetY();
                                break;
                            case Z:
                                z = moveDirection.getOffsetZ();
                            }

                            entity.setVelocity(x, y, z);
                        }

                        double dist = 0.0D;

                        for (int j = 0; j < boxes.size(); ++j) {
                            Box pistonVolume = expandIncrement(expandToProgress(boxes.get(j)), moveDirection, progressDelta);
                            Box entityVolume = entity.getBoundingBox();
                            if (pistonVolume.intersects(entityVolume)) {
                                dist = Math.max(dist, boxDelta(pistonVolume, moveDirection, entityVolume));
                                if (dist >= progressDelta) {
                                    break;
                                }
                            }
                        }

                        if (dist > 0.0D) {
                            dist = Math.min(dist, progressDelta) + 0.01D;
                            threadFace.set(moveDirection);
                            entity.move(MovementType.PISTON,
                                    new Vec3d(dist * moveDirection.getOffsetX(), dist * moveDirection.getOffsetY(), dist * moveDirection.getOffsetZ()));
                            threadFace.set(null);
                            if (!this.extending && this.source) {
                                this.moveEntity(entity, moveDirection, progressDelta);
                            }
                        }
                    }
                }

            }
        }
    }

    public Direction moveDirection() {
        return this.extending ? this.facing : this.facing.getOpposite();
    }

    private Box union(List<Box> list) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 1.0D;
        double maxY = 1.0D;
        double maxZ = 1.0D;

        Box box;
        for (Iterator<Box> it = list.iterator(); it.hasNext(); maxZ = Math.max(box.maxZ, maxZ)) {
            box = it.next();
            minX = Math.min(box.minX, minX);
            minY = Math.min(box.minY, minY);
            minZ = Math.min(box.minZ, minZ);
            maxX = Math.max(box.maxX, maxX);
            maxY = Math.max(box.maxY, maxY);
        }

        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private double boxDelta(Box fromBox, Direction towards, Box toBox) {
        switch (towards.getAxis()) {
        case X:
            return xDelta(fromBox, towards, toBox);
        case Y:
        default:
            return yDelta(fromBox, towards, toBox);
        case Z:
            return zDelta(fromBox, towards, toBox);
        }
    }

    private Box expandToProgress(Box box) {
        final double dist = normalize(nextProgress);
        return box.offset(pos.getX() + dist * facing.getOffsetX(), pos.getY() + dist * facing.getOffsetY(), pos.getZ() + dist * facing.getOffsetZ());
    }

    private Box expandIncrement(Box box, Direction towards, double dist) {
        final double offset = dist * towards.getDirection().offset();
        final double min = Math.min(offset, 0.0D);
        final double max = Math.max(offset, 0.0D);
        switch (towards) {
        case WEST:
            return new Box(box.minX + min, box.minY, box.minZ, box.minX + max, box.maxY, box.maxZ);
        case EAST:
            return new Box(box.maxX + min, box.minY, box.minZ, box.maxX + max, box.maxY, box.maxZ);
        case DOWN:
            return new Box(box.minX, box.minY + min, box.minZ, box.maxX, box.minY + max, box.maxZ);
        case UP:
        default:
            return new Box(box.minX, box.maxY + min, box.minZ, box.maxX, box.maxY + max, box.maxZ);
        case NORTH:
            return new Box(box.minX, box.minY, box.minZ + min, box.maxX, box.maxY, box.minZ + max);
        case SOUTH:
            return new Box(box.minX, box.minY, box.maxZ + min, box.maxX, box.maxY, box.maxZ + max);
        }
    }

    private void moveEntity(Entity entity, Direction towards, double dist) {
        final Box entityBox = entity.getBoundingBox();
        final Box posBox = VoxelShapes.fullCube().getBoundingBox().offset(pos);
        if (entityBox.intersects(posBox)) {
            final Direction away = towards.getOpposite();
            double scale = this.boxDelta(posBox, away, entityBox) + 0.01D;
            final double margin = this.boxDelta(posBox, away, entityBox.intersection(posBox)) + 0.01D;
            if (Math.abs(scale - margin) < 0.01D) {
                scale = Math.min(scale, dist) + 0.01D;
                threadFace.set(towards);
                entity.move(MovementType.PISTON, new Vec3d(scale * away.getOffsetX(), scale * away.getOffsetY(), scale * away.getOffsetZ()));
                threadFace.set(null);
            }
        }

    }

    private static double xDelta(Box fromBox, Direction face, Box toBox) {
        return face.getDirection() == AxisDirection.POSITIVE ? fromBox.maxX - toBox.minX : toBox.maxX - fromBox.minX;
    }

    private static double yDelta(Box fromBox, Direction face, Box toBox) {
        return face.getDirection() == AxisDirection.POSITIVE ? fromBox.maxY - toBox.minY : toBox.maxY - fromBox.minY;
    }

    private static double zDelta(Box fromBox, Direction directiofacen_1, Box toBox) {
        return directiofacen_1.getDirection() == AxisDirection.POSITIVE ? fromBox.maxZ - toBox.minZ : toBox.maxZ - fromBox.minZ;
    }

    public BlockState getPushedBlock() {
        return this.pushedBlock;
    }

    public void finish() {
        if (progress < 1.0F && world != null) {
            nextProgress = 1.0F;
            progress = nextProgress;
            world.removeBlockEntity(pos);
            invalidate();
            if (world.getBlockState(pos).getBlock() == PistonBlocks.MOVING_DOUBLE_PISTON) {
                BlockState endState;
                if (source) {
                    endState = Blocks.AIR.getDefaultState();
                } else {
                    endState = Block.getRenderingState(this.pushedBlock, this.world, this.pos);
                }

                this.world.setBlockState(this.pos, endState, 3);
                this.world.updateNeighbor(this.pos, endState.getBlock(), this.pos);
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
                    if (blockState_1.contains(Properties.WATERLOGGED) && (Boolean) blockState_1.get(Properties.WATERLOGGED)) {
                        blockState_1 = (BlockState) blockState_1.with(Properties.WATERLOGGED, false);
                    }

                    this.world.setBlockState(this.pos, blockState_1, 67);
                    this.world.updateNeighbor(this.pos, blockState_1.getBlock(), this.pos);
                }
            }

        } else {
            float progress = this.nextProgress + 0.5F;
            this.tickInner(progress);
            this.nextProgress = progress;
            if (this.nextProgress >= 1.0F) {
                this.nextProgress = 1.0F;
            }

        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        this.pushedBlock = TagHelper.deserializeBlockState(tag.getCompound("blockState"));
        this.facing = Direction.byId(tag.getInt("facing"));
        this.nextProgress = tag.getFloat("progress");
        this.progress = this.nextProgress;
        this.extending = tag.getBoolean("extending");
        this.source = tag.getBoolean("source");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.put("blockState", TagHelper.serializeBlockState(this.pushedBlock));
        tag.putInt("facing", this.facing.getId());
        tag.putFloat("progress", this.progress);
        tag.putBoolean("extending", this.extending);
        tag.putBoolean("source", this.source);
        return tag;
    }

    public VoxelShape getCollisionShape(BlockView blockView, BlockPos pos) {
        VoxelShape result;
        if (!extending && source) {
            result = pushedBlock.with(DoublePistonBlock.EXTENDED, true).getCollisionShape(blockView, pos);
        } else {
            result = VoxelShapes.empty();
        }

        final Direction face = threadFace.get();
        if (nextProgress < 1.0D && face == moveDirection()) {
            return result;
        } else {
            BlockState pushedShape;
            if (isSource()) {
                pushedShape = (PistonBlocks.DOUBLE_PISTON_HEAD.getDefaultState().with(DoublePistonHeadBlock.FACING, facing)).with(DoublePistonHeadBlock.SHORT,
                        extending != 1.0F - nextProgress < 4.0F);
            } else {
                pushedShape = pushedBlock;
            }

            final float offset = normalize(nextProgress);
            final double x = facing.getOffsetX() * offset;
            final double y = facing.getOffsetY() * offset;
            final double z = facing.getOffsetZ() * offset;
            return VoxelShapes.union(result, pushedShape.getCollisionShape(blockView, pos).offset(x, y, z));
        }
    }

    public long getSavedWorldTime() {
        return this.savedWorldTime;
    }
}
