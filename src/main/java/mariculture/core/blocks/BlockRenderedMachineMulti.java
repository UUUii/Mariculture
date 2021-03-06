package mariculture.core.blocks;

import mariculture.Mariculture;
import mariculture.api.core.MaricultureTab;
import mariculture.core.blocks.base.BlockFunctionalMulti;
import mariculture.core.events.MaricultureEvents;
import mariculture.core.helpers.FluidHelper;
import mariculture.core.helpers.cofh.CoFhItemHelper;
import mariculture.core.items.ItemUpgrade;
import mariculture.core.lib.MachineRenderedMultiMeta;
import mariculture.core.lib.Modules;
import mariculture.core.lib.RenderIds;
import mariculture.core.lib.UpgradeMeta;
import mariculture.core.network.PacketHandler;
import mariculture.core.tile.TileVat;
import mariculture.fishery.tile.TileSifter;
import mariculture.lib.helpers.ItemHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRenderedMachineMulti extends BlockFunctionalMulti {
    public IIcon bar1;

    public BlockRenderedMachineMulti() {
        super(Material.iron);
        setLightOpacity(0);
    }

    @Override
    public String getToolType(int meta) {
        return meta == MachineRenderedMultiMeta.SIFTER ? "axe" : "pickaxe";
    }

    @Override
    public int getToolLevel(int meta) {
        if (meta == MachineRenderedMultiMeta.SIFTER) return 0;
        return meta == MachineRenderedMultiMeta.VAT ? 1 : 2;
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        switch (meta) {
            case MachineRenderedMultiMeta.VAT:
                return 2.5F;
            case MachineRenderedMultiMeta.SIFTER:
                return 1.5F;
        }

        return MaricultureEvents.getBlockHardness(this, meta, 5F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return false;

        ItemStack heldItem = player.getCurrentEquippedItem();
        if (tile instanceof TileVat) {
            TileVat vat = (TileVat) tile;
            ItemStack held = player.getCurrentEquippedItem();
            ItemStack input = vat.getStackInSlot(0);
            ItemStack output = vat.getStackInSlot(1);
            if (FluidHelper.isFluidOrEmpty(player.getCurrentEquippedItem())) return FluidHelper.handleFillOrDrain((IFluidHandler) world.getTileEntity(x, y, z), player, ForgeDirection.UP);

            if (output != null) {
                if (!player.inventory.addItemStackToInventory(vat.getStackInSlot(1))) if (!world.isRemote) {
                    ItemHelper.spawnItem(world, x, y + 1, z, vat.getStackInSlot(1));
                }

                vat.setInventorySlotContents(1, null);

                return true;
            } else if (player.isSneaking() && input != null) {
                if (!player.inventory.addItemStackToInventory(vat.getStackInSlot(0))) if (!world.isRemote) {
                    ItemHelper.spawnItem(world, x, y + 1, z, vat.getStackInSlot(0));
                }

                vat.setInventorySlotContents(0, null);

                return true;
            } else if (held != null && !player.isSneaking()) if (input == null) {
                if (!world.isRemote) {
                    ItemStack copy = held.copy();
                    copy.stackSize = 1;
                    vat.setInventorySlotContents(0, copy);
                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                }

                return true;
            } else if (CoFhItemHelper.areItemStacksEqualNoNBT(input, held)) {
                if (input.stackSize + 1 < input.getMaxStackSize()) {
                    if (!world.isRemote) {
                        ItemStack stack = input.copy();
                        stack.stackSize++;
                        vat.setInventorySlotContents(0, stack);
                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    }
                    return true;
                } else return false;
            } else return false;
        }

        if (tile instanceof TileSifter) {
            TileSifter sifter = ((TileSifter) tile).getMaster();
            if (sifter != null) {
                if (sifter.hasInventory) {
                    player.openGui(Mariculture.instance, 0, world, sifter.xCoord, sifter.yCoord, sifter.zCoord);
                } else {
                    if (heldItem != null) {
                        if (heldItem.getItem() instanceof ItemUpgrade && heldItem.getItemDamage() == UpgradeMeta.BASIC_STORAGE && sifter.hasInventory == false) {
                            sifter.hasInventory = true;
                            if (!player.capabilities.isCreativeMode) {
                                player.inventory.decrStackSize(player.inventory.currentItem, 1);
                            }

                            if (!world.isRemote) {
                                PacketHandler.updateRender(sifter);
                            }
                        } else {
                            ItemStack addition = heldItem.copy();
                            addition.stackSize = 1;
                            if (sifter.process(addition, world.rand)) {
                                if (!player.capabilities.isCreativeMode) {
                                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (entity instanceof EntityItem && !world.isRemote) {
            if (tile instanceof TileSifter) {
                TileSifter sifter = ((TileSifter) tile).getMaster();
                if (sifter != null) {
                    EntityItem item = (EntityItem) entity;
                    ItemStack stack = item.getEntityItem();
                    if (sifter.process(stack, world.rand)) {
                        item.setDead();
                    }
                }
            }
        }
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RenderIds.RENDER_ALL;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
        int meta = block.getBlockMetadata(x, y, z);
        switch (meta) {
            case MachineRenderedMultiMeta.COMPRESSOR_TOP:
                setBlockBounds(0.05F, 0F, 0.05F, 0.95F, 0.15F, 0.95F);
                break;
            case MachineRenderedMultiMeta.COMPRESSOR_BASE:
                setBlockBounds(0.05F, 0F, 0.05F, 0.95F, 1F, 0.95F);
                break;
            default:
                setBlockBounds(0F, 0F, 0F, 1F, 0.95F, 1F);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == MachineRenderedMultiMeta.VAT) return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, (double) y + 0.50001F, z + maxZ);

        return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
    }

    @Override
    public boolean destroyBlock(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        switch (meta) {
            case MachineRenderedMultiMeta.VAT:
                return new TileVat();
            case MachineRenderedMultiMeta.SIFTER:
                return new TileSifter();
        }

        return MaricultureEvents.getTileEntity(this, meta, null);
    }

    @Override
    public boolean isActive(int meta) {
        switch (meta) {
            case MachineRenderedMultiMeta.VAT:
                return true;
            case MachineRenderedMultiMeta.SIFTER:
                return Modules.isActive(Modules.fishery);
        }

        return MaricultureEvents.isActive(this, meta, false);
    }

    @Override
    public boolean isValidTab(CreativeTabs tab, int meta) {
        if (meta == MachineRenderedMultiMeta.SIFTER) return MaricultureTab.tabFishery == tab;
        return tab == MaricultureTab.tabFactory;
    }

    @Override
    public int getMetaCount() {
        return MachineRenderedMultiMeta.COUNT;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        String name = prefix != null ? prefix : "";
        icons = new IIcon[getMetaCount()];

        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(MaricultureEvents.getMod(this, i, "mariculture") + ":" + name + getName(i));
        }
        
        bar1 = iconRegister.registerIcon(Mariculture.modid + ":bar1");
    }
}
