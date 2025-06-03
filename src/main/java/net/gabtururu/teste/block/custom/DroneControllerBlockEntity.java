//package net.gabtururu.teste.block.custom;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class DroneControllerBlockEntity extends BlockEntity {
//
//    private UUID droneUUID; // Identifica o drone que está nesse bloco
//    private List<BlockPos> destinos = new ArrayList<>();
//
//    public DroneControllerBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> type) {
//        super(type, pos, state);
//    }
//
//    public void setDroneUUID(UUID uuid) {
//        this.droneUUID = uuid;
//        setChanged();
//    }
//
//    public UUID getDroneUUID() {
//        return droneUUID;
//    }
//
//    public List<BlockPos> getDestinos() {
//        return destinos;
//    }
//
//    public void addDestino(BlockPos pos) {
//        if (!destinos.contains(pos)) {
//            destinos.add(pos);
//            setChanged();
//        }
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        super.load(tag);
//        if (tag.hasUUID("DroneUUID")) {
//            droneUUID = tag.getUUID("DroneUUID");
//        }
//        destinos.clear();
//        if (tag.contains("Destinos")) {
//            ListTag list = tag.getList("Destinos", 10); // TAG_COMPOUND
//            for (int i = 0; i < list.size(); i++) {
//                CompoundTag posTag = list.getCompound(i);
//                int x = posTag.getInt("X");
//                int y = posTag.getInt("Y");
//                int z = posTag.getInt("Z");
//                destinos.add(new BlockPos(x, y, z));
//            }
//        }
//    }
//
//    @Override
//    public void saveAdditional(CompoundTag tag) {
//        super.saveAdditional(tag);
//        if (droneUUID != null) {
//            tag.putUUID("DroneUUID", droneUUID);
//        }
//        ListTag list = new ListTag();
//        for (BlockPos pos : destinos) {
//            CompoundTag posTag = new CompoundTag();
//            posTag.putInt("X", pos.getX());
//            posTag.putInt("Y", pos.getY());
//            posTag.putInt("Z", pos.getZ());
//            list.add(posTag);
//        }
//        tag.put("Destinos", list);
//    }
//}
