package joshie.mariculture.core.blocks;

import java.util.Random;

import joshie.lib.helpers.ItemHelper;
import joshie.mariculture.Mariculture;
import joshie.mariculture.api.core.MaricultureTab;
import joshie.mariculture.core.Core;
import joshie.mariculture.core.blocks.base.BlockConnected;
import joshie.mariculture.core.handlers.FluidDicHandler;
import joshie.mariculture.core.helpers.BlockHelper;
import joshie.mariculture.core.helpers.FluidHelper;
import joshie.mariculture.core.lib.BottleMeta;
import joshie.mariculture.core.lib.Modules;
import joshie.mariculture.core.lib.RenderIds;
import joshie.mariculture.core.lib.TankMeta;
import joshie.mariculture.core.network.PacketHandler;
import joshie.mariculture.core.tile.TileTankBlock;
import joshie.mariculture.core.tile.TileVoidBottle;
import joshie.mariculture.factory.tile.TileDictionaryFluid;
import joshie.mariculture.fishery.tile.TileFishTank;
import joshie.mariculture.fishery.tile.TileHatchery;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTank extends BlockConnected {
    private static IIcon[] fishTank = new IIcon[47];

    public BlockTank() {
        super(Material.piston);
    }

    @Override
    public String getToolType(int meta) {
        return meta == TankMeta.DIC ? "axe" : null;
    }

    @Override
    public int getToolLevel(int meta) {
        return meta == TankMeta.DIC ? 1 : 0;
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        switch (world.getBlockMetadata(x, y, z)) {
            case TankMeta.BOTTLE:
                return 0.1F;
            case TankMeta.TANK:
                return 0.5F;
            case TankMeta.FISH:
                return 1.0F;
            case TankMeta.DIC:
            case TankMeta.HATCHERY:
                return 1.5F;
            default:
                return 1.0F;
        }
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RenderIds.RENDER_ALL;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float clickX, float clickY, float clickZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return false;

        if (tile instanceof TileFishTank) if (player.isSneaking()) return false;
        else {
            player.openGui(Mariculture.instance, -1, world, x, y, z);
            return true;
        }

        if (tile instanceof TileDictionaryFluid) if (player.isSneaking()) return false;
        else {
            TileDictionaryFluid dic = (TileDictionaryFluid) tile;
            String next = FluidDicHandler.getNext(dic.getFluid());
            Fluid fluid = FluidRegistry.getFluid(next);
            if (fluid != null) {
                dic.tank.setFluid(new FluidStack(fluid, 1));
                PacketHandler.syncFluids(dic, dic.tank.getFluid());
            }
        }

        if (tile instanceof TileHatchery) {
            TileHatchery hatchery = (TileHatchery) tile;
            if (hatchery.getStackInSlot(0) != null && player.isSneaking()) {
                if (!world.isRemote) {
                    PacketHandler.syncInventory(hatchery, hatchery.getInventory());
                }

                ItemHelper.addToPlayerInventory(player, world, x, y + 1, z, hatchery.getStackInSlot(0));
                hatchery.setInventorySlotContents(0, null);
            } else if (player.getCurrentEquippedItem() != null && hatchery.getStackInSlot(0) == null) {
                ItemStack stack = player.getCurrentEquippedItem().copy();
                stack.stackSize = 1;
                hatchery.setInventorySlotContents(0, stack);
                if (!player.capabilities.isCreativeMode) {
                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                }
            } else {
                for (int i = 1; i < hatchery.getInventory().length; i++) {
                    if (hatchery.getStackInSlot(i) != null) {
                        if (!world.isRemote) {
                            PacketHandler.syncInventory(hatchery, hatchery.getInventory());
                        }

                        ItemHelper.addToPlayerInventory(player, world, x, y + 1, z, hatchery.getStackInSlot(i));
                        hatchery.setInventorySlotContents(i, null);
                    }
                }
            }

            return true;
        }

        return FluidHelper.handleFillOrDrain((IFluidHandler) world.getTileEntity(x, y, z), player, ForgeDirection.UP);
    }

    @Override
    public boolean hasTileEntity(int meta) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        switch (meta) {
            case TankMeta.BOTTLE:
                return new TileVoidBottle();
            case TankMeta.TANK:
                return new TileTankBlock();
            case TankMeta.FISH:
                return new TileFishTank();
            case TankMeta.DIC:
                return new TileDictionaryFluid();
            case TankMeta.HATCHERY:
                return new TileHatchery();
            default:
                return null;
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileTankBlock) {
            TileTankBlock tank = (TileTankBlock) tile;
            FluidStack fluid = tank.getFluid();
            if (fluid != null) return fluid.getFluid().getLuminosity();
        }

        return 0;
    }

    @Override
    public int getMetaCount() {
        return TankMeta.COUNT;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
        if (block.getBlockMetadata(x, y, z) != TankMeta.BOTTLE) {
            setBlockBounds(0F, 0F, 0F, 1F, 1F, 1F);
        } else {
            setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z) == TankMeta.BOTTLE ? null : super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int j) {
        switch (meta) {
            case TankMeta.TANK:
                return null;
            case TankMeta.BOTTLE:
                return Core.bottles;
            default:
                return super.getItemDropped(meta, random, j);
        }
    }

    @Override
    public int damageDropped(int meta) {
        switch (meta) {
            case TankMeta.BOTTLE:
                return BottleMeta.VOID;
            default:
                return meta;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int facing = BlockPistonBase.determineOrientation(world, x, y, z, entity);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return;
        if (tile instanceof TileFishTank) {
            ((TileFishTank) tile).orientation = ForgeDirection.getOrientation(facing);
        }

        if (stack.hasTagCompound()) if (world.getBlockMetadata(x, y, z) == TankMeta.TANK) if (tile instanceof TileTankBlock) {
            TileTankBlock tank = (TileTankBlock) tile;
            tank.setFluid(FluidStack.loadFluidStackFromNBT(stack.stackTagCompound));
            PacketHandler.syncFluids(tank, tank.getFluid());
        }
    }

    //Change back to remove block block by player instead of getDrops for everything
    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == TankMeta.TANK && !player.capabilities.isCreativeMode) {
            ItemStack drop = new ItemStack(Core.tanks, 1, TankMeta.TANK);
            TileTankBlock tank = (TileTankBlock) world.getTileEntity(x, y, z);
            if (tank != null && tank.getFluid() != null) {
                if (!drop.hasTagCompound()) {
                    drop.setTagCompound(new NBTTagCompound());
                }

                tank.getFluid().writeToNBT(drop.stackTagCompound);
            }

            ItemHelper.spawnItem(world, x, y, z, drop);
            return world.setBlockToAir(x, y, z);
        } else return world.setBlockToAir(x, y, z);
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == TankMeta.BOTTLE) {
            for (int j = 0; j < 2; ++j) {
                float f = rand.nextFloat() - rand.nextFloat();
                float f1 = rand.nextFloat() - rand.nextFloat();
                float f2 = rand.nextFloat() - rand.nextFloat();
                world.spawnParticle("magicCrit", x + 0.5D + f, y + 0.5D + f1, z + 0.5D + f2, 0, 0, 0);
                world.spawnParticle("witchMagic", x + 0.5D, y + 0.5D, z + 0.5D, 0, 0, 0);
            }
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        ItemStack drop = new ItemStack(Core.tanks, 1, meta);
        if (meta == TankMeta.TANK) {
            TileTankBlock tank = (TileTankBlock) world.getTileEntity(x, y, z);
            if (tank != null && tank.getFluid() != null) {
                if (!drop.hasTagCompound()) {
                    drop.setTagCompound(new NBTTagCompound());
                }

                tank.getFluid().writeToNBT(drop.stackTagCompound);
            }
        } else if (meta == TankMeta.BOTTLE) {
            drop = new ItemStack(Core.bottles, 1, BottleMeta.VOID);
        }

        return drop;
    }

    @Override
    public boolean isActive(int meta) {
        switch (meta) {
            case TankMeta.BOTTLE:
                return false;
            case TankMeta.FISH:
            case TankMeta.HATCHERY:
                return Modules.isActive(Modules.fishery);
            case TankMeta.DIC:
                return false;
            default:
                return true;
        }
    }

    @Override
    public boolean isValidTab(CreativeTabs tab, int meta) {
        switch (meta) {
            case TankMeta.HATCHERY:
            case TankMeta.FISH:
                return tab == MaricultureTab.tabFishery;
            default:
                return tab == MaricultureTab.tabFactory;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int j) {
        BlockHelper.dropFish(world, x, y, z);
        super.breakBlock(world, x, y, z, block, j);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[getMetaCount()];

        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(Mariculture.modid + ":" + getName(i) + "Tank");
        }

        registerConnectedTextures(iconRegister);
    }

    @Override
    public IIcon[] getTexture(int meta) {
        return meta == TankMeta.FISH ? fishTank : null;
    }

    @Override
    public void registerConnectedTextures(IIconRegister iconRegister) {
        for (int i = 0; i < 47; i++) {
            fishTank[i] = iconRegister.registerIcon(Mariculture.modid + ":fishTank/" + (i + 1));
        }
    }
}