package fr.cyrilneveu.transit.command;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fr.cyrilneveu.transit.TransitTags.MODID;

public final class TransitCommand extends CommandBase {
    private final List<String> aliases;

    public TransitCommand() {
        this.aliases = Lists.newArrayList(MODID, "transit");
    }

    @Override
    public String getName() {
        return "transit";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/transit <id> OR /transit destinations";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(2, getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            List<String> dimensions = new ArrayList<>();
            dimensions.add("destinations");
            Arrays.stream(DimensionManager.getStaticDimensionIDs()).map(Object::toString).forEachOrdered(dimensions::add);
            return getListOfStringsMatchingLastWord(args, dimensions);
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + getUsage(sender)));
            return;
        }

        try {
            if ("destinations".equalsIgnoreCase(args[0])) {
                for (Integer id : DimensionManager.getStaticDimensionIDs()) {
                    DimensionType type = DimensionManager.getProviderType(id);
                    sender.sendMessage(new TextComponentString(type.getName() + ": " + TextFormatting.BLUE + id + TextFormatting.RESET));
                }

                return;
            }

            int dimId = Integer.parseInt(args[0]);

            if (DimensionManager.isDimensionRegistered(dimId)) {
                if (DimensionManager.getWorld(dimId) == null)
                    DimensionManager.initDimension(dimId);

                EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
                if (player == null) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Must be a player to use this command!"));
                    return;
                }

                player.getServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, dimId, new CustomTeleporter(DimensionManager.getWorld(dimId)));
            } else sender.sendMessage(new TextComponentString(TextFormatting.RED + "Dimension does not exists!"));
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error parsing dimension!"));
        }
    }

    private static class CustomTeleporter extends Teleporter {
        public CustomTeleporter(WorldServer worldIn) {
            super(worldIn);
        }

        public void teleport(Entity entity, WorldServer world) {
            if (entity.isEntityAlive()) {
                entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                world.spawnEntity(entity);
                world.updateEntityWithOptionalForce(entity, false);
            }

            entity.setWorld(world);
        }

        @Override
        public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
            double x, y, z;
            x = entityIn.posX;
            y = entityIn.posY;
            z = entityIn.posZ;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int yy = (int) y; yy < world.getHeight(); yy++) {
                pos.setPos(x, yy, z);
                if (world.isAirBlock(pos) && world.isAirBlock(pos.add(0, 1, 0))) {
                    y = yy;
                    break;
                }
            }

            if (entityIn instanceof EntityPlayerMP)
                ((EntityPlayerMP) entityIn).connection.setPlayerLocation(x, y, z, entityIn.rotationYaw, entityIn.rotationPitch);
            else entityIn.setLocationAndAngles(x, y, z, entityIn.rotationYaw, entityIn.rotationPitch);

            return true;
        }

        @Override
        public void removeStalePortalLocations(long par1) {

        }

        @Override
        public boolean makePortal(Entity entity) {
            return true;
        }
    }
}
