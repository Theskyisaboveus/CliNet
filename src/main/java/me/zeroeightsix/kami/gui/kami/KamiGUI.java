package me.zeroeightsix.kami.gui.kami;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.gui.kami.component.*;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiTheme;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by 086 on 25/06/2017.
 * Welcomer update by Hamburger on 14/01/2020
 */
public class KamiGUI extends GUI {

    public static final RootFontRenderer fontRenderer = new RootFontRenderer(1);
    public Theme theme;

    public static String selectedTheme;
    public static int selectedThemeIndex = 0;
    public static String selectedArrayColour;

    public static Double[] arrayColour = new Double[] {255d, 167d, 35d};

    public static ColourHolder primaryColour = new ColourHolder(29, 29, 29);

    public KamiGUI() {
        super(new KamiTheme());
        theme = getTheme();
    }

    @Override
    public void drawGUI() {
        super.drawGUI();
    }

    @Override
    public void initializeGUI() {
        HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>> categoryScrollpaneHashMap = new HashMap<>();
        for (Module module : ModuleManager.getModules()) {
            if (module.getCategory().isHidden()) continue;
            Module.Category moduleCategory = module.getCategory();
            if (!categoryScrollpaneHashMap.containsKey(moduleCategory)) {
                Stretcherlayout stretcherlayout = new Stretcherlayout(1);
                stretcherlayout.setComponentOffsetWidth(0);
                Scrollpane scrollpane = new Scrollpane(getTheme(), stretcherlayout, 300, 260);
                scrollpane.setMaximumHeight(180);
                categoryScrollpaneHashMap.put(moduleCategory, new Pair<>(scrollpane, new SettingsPanel(getTheme(), null)));
            }

            Pair<Scrollpane, SettingsPanel> pair = categoryScrollpaneHashMap.get(moduleCategory);
            Scrollpane scrollpane = pair.getKey();
            CheckButton checkButton = new CheckButton(module.getName());
            checkButton.setToggled(module.isEnabled());

            checkButton.addTickListener(() -> { // dear god
                checkButton.setToggled(module.isEnabled());
                checkButton.setName(module.getName());
            });

            checkButton.addMouseListener(new MouseListener() {
                @Override
                public void onMouseDown(MouseButtonEvent event) {
                    if (event.getButton() == 1) { // Right click
                        pair.getValue().setModule(module);
                        pair.getValue().setX(event.getX() + checkButton.getX());
                        pair.getValue().setY(event.getY() + checkButton.getY());
                    }
                }

                @Override
                public void onMouseRelease(MouseButtonEvent event) {

                }

                @Override
                public void onMouseDrag(MouseButtonEvent event) {

                }

                @Override
                public void onMouseMove(MouseMoveEvent event) {

                }

                @Override
                public void onScroll(MouseScrollEvent event) {

                }
            });
            checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                @Override
                public void execute(CheckButton component, CheckButtonPoofInfo info) {
                    if (info.getAction().equals(CheckButton.CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)) {
                        module.setEnabled(checkButton.isToggled());
                    }
                }
            });
            scrollpane.addChild(checkButton);
        }

        int x = 10;
        int y = 10;
        int nexty = y;
        for (Map.Entry<Module.Category, Pair<Scrollpane, SettingsPanel>> entry : categoryScrollpaneHashMap.entrySet()) {
            Stretcherlayout stretcherlayout = new Stretcherlayout(1);
            stretcherlayout.COMPONENT_OFFSET_Y = 1;
            Frame frame = new Frame(getTheme(), stretcherlayout, entry.getKey().getName());
            Scrollpane scrollpane = entry.getValue().getKey();
            frame.addChild(scrollpane);
            frame.addChild(entry.getValue().getValue());
            scrollpane.setOriginOffsetY(0);
            scrollpane.setOriginOffsetX(0);
            frame.setCloseable(false);

            frame.setX(x);
            frame.setY(y);

            addChild(frame);

            nexty = Math.max(y + frame.getHeight() + 10, nexty);
            x += frame.getWidth() + 10;
            if (x > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
            }
        }

        this.addMouseListener(new MouseListener() {
            private boolean isBetween(int min, int val, int max) {
                return !(val > max || val < min);
            }

            @Override
            public void onMouseDown(MouseButtonEvent event) {
                List<SettingsPanel> panels = ContainerHelper.getAllChildren(SettingsPanel.class, KamiGUI.this);
                for (SettingsPanel settingsPanel : panels) {
                    if (!settingsPanel.isVisible()) continue;
                    int[] real = GUI.calculateRealPosition(settingsPanel);
                    int pX = event.getX() - real[0];
                    int pY = event.getY() - real[1];
                    if (!isBetween(0, pX, settingsPanel.getWidth()) || !isBetween(0, pY, settingsPanel.getHeight()))
                        settingsPanel.setVisible(false);
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });

        ArrayList<Frame> frames = new ArrayList<>();

        Frame frame = new Frame(getTheme(), new Stretcherlayout(1), "Active modules");
        frame.setCloseable(false);
        frame.addChild(new ActiveModules());
        frame.setPinneable(true);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Info");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label information = new Label("");
        information.setShadow(true);
        information.addTickListener(() -> {
            information.setText("");
            information.addLine("\u00A7b" + KamiMod.CLINET_PREFIX + "\u00A73 " + KamiMod.MODVER);
            information.addLine("\u00A7b" + Math.round(LagCompensator.INSTANCE.getTickRate()) + Command.SECTIONSIGN() + "3 tps");
            information.addLine("\u00A7b" + Wrapper.getMinecraft().debugFPS + Command.SECTIONSIGN() + "3 fps");
            information.addLine("[ " + Command.SECTIONSIGN() +"3" + EntityUtil.getPlayerSpeed() + "m/s " +  Command.SECTIONSIGN() + "r]");
            information.addLine("\u00A7b" + EntityUtil.getPing() + Command.SECTIONSIGN() + "3 ms");


        });
        frame.addChild(information);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Friends");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label friends = new Label("");
        friends.setShadow(true);
        friends.addTickListener(() -> {
            friends.setText("");
            Friends.friends.getValue().forEach(friend -> {
                friends.addLine(friend.getUsername());
            });
        });
        frame.addChild(friends);
        friends.setFontRenderer(fontRenderer);
        frames.add(frame);


        frame = new Frame(getTheme(), new Stretcherlayout(1), "Totems");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label totem = new Label("");
        totem.setShadow(true);
        totem.addTickListener(() -> {
            totem.setText("");
            int totemCount = 0;
            for (int i=0; i < 45; i++) {
                ItemStack itemStack = Wrapper.getMinecraft().player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                    totemCount += itemStack.stackSize;
                }
            }
            totem.addLine("Totems: " + String.valueOf(totemCount));
        });
        frame.addChild(totem);
        totem.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Crystals");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label crystals = new Label("");
        crystals.setShadow(true);
        crystals.addTickListener(() -> {
            crystals.setText("");
            int crystalCount = 0;
            for (int i=0; i < 45; i++) {
                ItemStack itemStack = Wrapper.getMinecraft().player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.END_CRYSTAL) {
                    crystalCount += itemStack.stackSize;
                }
            }
            crystals.addText("Crystals: " + String.valueOf(crystalCount));
        });
        frame.addChild(crystals);
        crystals.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Gapples");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label gapples = new Label("");
        gapples.setShadow(true);
        gapples.addTickListener(() -> {
            gapples.setText("");
            int gappleCount = 0;
            for (int i=0; i < 45; i++) {
                ItemStack itemStack = Wrapper.getMinecraft().player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.GOLDEN_APPLE && itemStack.getItemDamage() == 1) {
                    gappleCount += itemStack.stackSize;
                }
            }
            gapples.addText("Gapples: " + String.valueOf(gappleCount));
        });
        frame.addChild(gapples);
        gapples.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "EXP");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label xp = new Label("");
        xp.setShadow(true);
        xp.addTickListener(() -> {
            xp.setText("");
            int xpCount = 0;
            for(int i = 0; i < 45; i++) {
                ItemStack itemStack = Wrapper.getMinecraft().player.inventory.getStackInSlot(i);
                if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE) {
                    xpCount += itemStack.stackSize;
                }
            }
            xp.addText("XP: " + String.valueOf(xpCount));
        });
        frame.addChild(xp);
        xp.setFontRenderer(fontRenderer);
        frames.add(frame);


        frame = new Frame(getTheme(), new Stretcherlayout(1), "Item Durability");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label durability = new Label("");
        durability.setShadow(true);
        durability.addTickListener(() -> {
            ItemStack heldItem = Wrapper.getMinecraft().player.getHeldItemMainhand();
            durability.setText("");
            durability.addLine(String.valueOf(heldItem.getMaxDamage() - heldItem.getItemDamage()));
        });
        frame.addChild(durability);
        durability.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Welcomer");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label nameinfo = new Label("");
        nameinfo.setShadow(true);
        nameinfo.addTickListener(() -> {
                    nameinfo.setText("");
                    nameinfo.addLine("Welcome, " + Minecraft.getMinecraft().getSession().getUsername() + " :)");
                });
        frame.addChild(nameinfo);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Watermark");
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(75);
        Label watermark = new Label("CliNet");
        watermark.setX((frame.getWidth() / 2));
        watermark.setShadow(true);
        frame.addChild(watermark);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "GUI");
        frame.setCloseable(false);
        frame.setPinneable(false);
        EnumButton theme = new EnumButton("Theme", new String[] {"Modern", "Modern2", "Kami", "Kami Blue"});
        //ColourPickerButtonRainbow other = new ColourPickerButtonRainbow("ArrayList");
        Slider rArrayListColour = new Slider(arrayColour[0], 0, 255, 1, "Red ArrayList", true);
        Slider gArrayListColour = new Slider(arrayColour[1], 0, 255, 1, "Green ArrayList", true);
        Slider bArrayListColour = new Slider(arrayColour[2], 0, 255, 1, "Blue ArrayList", true);
        ColorizedCheckButton RB = new ColorizedCheckButton("Rainbow ArrayList");
        theme.addTickListener(() -> {
            selectedTheme = theme.getIndexMode();
            selectedThemeIndex = theme.getIndex();

        });
        RB.addTickListener(() -> {
            if (RB.isToggled()) {
                this.selectedArrayColour = "RB";
            } else {
                this.selectedArrayColour = "CUSTOM";
            }
            this.arrayColour = new Double[]{rArrayListColour.getValue(), gArrayListColour.getValue(), bArrayListColour.getValue()};
        });
        frame.addChild(theme);
        frame.addChild(rArrayListColour);
        frame.addChild(gArrayListColour);
        frame.addChild(bArrayListColour);
        frame.addChild(RB);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Clock");
        frame.setCloseable(false);
        frame.setMinimizeable(false);
        frame.setPinneable(true);
        Label clock = new Label("");
        clock.setShadow(true);
        clock.addTickListener(() -> {
            clock.setText(DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()));
        });
        frame.addChild(clock);
        clock.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Inventory Viewer");
        frame.setCloseable(false);
        frame.setMinimizeable(false);
        frame.setPinneable(true);
        Label inventory = new Label("");
        inventory.setShadow(true);
        inventory.addTickListener(() -> {
            inventory.setText("      ");
        });
        frame.addChild(inventory);
        inventory.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Text Radar");
        Label list = new Label("");
        DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder healthSB = new StringBuilder();
        list.addTickListener(() -> {
            if (!list.isVisible()) return;
            list.setText("");

            Minecraft mc = Wrapper.getMinecraft();

            if (mc.player == null) return;
            List<EntityPlayer> entityList = mc.world.playerEntities;

            Map<String, Integer> players = new HashMap<>();
            for (Entity e : entityList) {
                if (e.getName().equals(mc.player.getName())) continue;
                String posString = (e.posY > mc.player.posY ? ChatFormatting.DARK_GREEN + "+" : (e.posY == mc.player.posY ? " " : ChatFormatting.DARK_RED + "-"));

                String strengthfactor = "";
                EntityPlayer eplayer = (EntityPlayer) e;
                if (eplayer.isPotionActive(MobEffects.STRENGTH) && ModuleManager.isModuleEnabled("StrengthDetect")) {
                    strengthfactor = "S";
                }
                float hpRaw = ((EntityLivingBase) e).getHealth() + ((EntityLivingBase) e).getAbsorptionAmount();
                String hp = dfHealth.format(hpRaw);
                healthSB.append(Command.SECTIONSIGN());
                if (hpRaw >= 20) {
                    healthSB.append("a");
                } else if (hpRaw >= 10) {
                    healthSB.append("e");
                } else if (hpRaw >= 5) {
                    healthSB.append("6");
                } else {
                    healthSB.append("c");
                }
                healthSB.append(hp);
                players.put(ChatFormatting.GRAY + posString + " " + healthSB.toString() + " " + ChatFormatting.RED +  strengthfactor + (strengthfactor.equals("S") ? " " : "") + (Friends.isFriend(e.getName()) ? ChatFormatting.BLUE : ChatFormatting.GRAY) + e.getName(), (int) mc.player.getDistance(e));
                healthSB.setLength(0);
            }

            if (players.isEmpty()) {
                list.setText("");
                return;
            }

            players = sortByValue(players);

            for (Map.Entry<String, Integer> player : players.entrySet()) {
                list.addLine(Command.SECTIONSIGN() + "7" + player.getKey() + " " + Command.SECTIONSIGN() + "8" + player.getValue());
            }
        });
        frame.setCloseable(false);
        frame.setPinneable(true);
        frame.setMinimumWidth(75);
        list.setShadow(true);
        frame.addChild(list);
        list.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Entities");
        Label entityLabel = new Label("");
        frame.setCloseable(false);
        entityLabel.addTickListener(new TickListener() {
            Minecraft mc = Wrapper.getMinecraft();

            @Override
            public void onTick() {
                if (mc.player == null || !entityLabel.isVisible()) return;

                final List<Entity> entityList = new ArrayList<>(mc.world.loadedEntityList);
                if (entityList.size() <= 1) {
                    entityLabel.setText("");
                    return;
                }
                final Map<String, Integer> entityCounts = entityList.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> !(e instanceof EntityPlayer))
                        .collect(Collectors.groupingBy(KamiGUI::getEntityName,
                                Collectors.reducing(0, ent -> {
                                    if (ent instanceof EntityItem)
                                        return ((EntityItem)ent).getItem().getCount();
                                    return 1;
                                }, Integer::sum)
                        ));

                entityLabel.setText("");
                entityCounts.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(entry -> TextFormatting.GRAY + entry.getKey() + " " + TextFormatting.DARK_GRAY + "x" + entry.getValue())
                        .forEach(entityLabel::addLine);

                //entityLabel.getParent().setHeight(entityLabel.getLines().length * (entityLabel.getTheme().getFontRenderer().getFontHeight()+1) + 3);
            }
        });
        frame.addChild(entityLabel);
        frame.setPinneable(true);
        entityLabel.setShadow(true);
        entityLabel.setFontRenderer(fontRenderer);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Coordinates");
        frame.setCloseable(false);
        frame.setPinneable(true);
        Label coordsLabel = new Label("");
        coordsLabel.addTickListener(new TickListener() {
            Minecraft mc = Minecraft.getMinecraft();

            @Override
            public void onTick() {
                boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

                int posX = (int) mc.player.posX;
                int posY = (int) mc.player.posY;
                int posZ = (int) mc.player.posZ;

                float f = !inHell ? 0.125f : 8;
                int hposX = (int) (mc.player.posX * f);
                int hposZ = (int) (mc.player.posZ * f);

                coordsLabel.setText(String.format(" %sf%,d%s7, %sf%,d%s7, %sf%,d %s7(%sf%,d%s7, %sf%,d%s7, %sf%,d%s7)",
                        Command.SECTIONSIGN(),
                        posX,
                        Command.SECTIONSIGN(),
                        Command.SECTIONSIGN(),
                        posY,
                        Command.SECTIONSIGN(),
                        Command.SECTIONSIGN(),
                        posZ,
                        Command.SECTIONSIGN(),
                        Command.SECTIONSIGN(),
                        hposX,
                        Command.SECTIONSIGN(),
                        Command.SECTIONSIGN(),
                        posY,
                        Command.SECTIONSIGN(),
                        Command.SECTIONSIGN(),
                        hposZ,
                        Command.SECTIONSIGN()
                ));
            }
        });
        frame.addChild(coordsLabel);
        coordsLabel.setFontRenderer(fontRenderer);
        coordsLabel.setShadow(true);
        frame.setHeight(20);
        frames.add(frame);

        frame = new Frame(getTheme(), new Stretcherlayout(1), "Radar");
        frame.setCloseable(false);
        frame.setMinimizeable(true);
        frame.setPinneable(true);
        frame.addChild(new Radar());
        frame.setWidth(100);
        frame.setHeight(100);
        frames.add(frame);

        for (Frame frame1 : frames) {
            frame1.setX(x);
            frame1.setY(y);

            nexty = Math.max(y + frame1.getHeight() + 10, nexty);
            x += frame1.getWidth() + 10;
            if (x * DisplayGuiScreen.getScale() > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
                x = 10;
            }

            addChild(frame1);
        }
    }

    private static String getEntityName(@Nonnull Entity entity) {
        if (entity instanceof EntityItem) {
            return TextFormatting.DARK_AQUA + ((EntityItem) entity).getItem().getItem().getItemStackDisplayName(((EntityItem) entity).getItem());
        }
        if (entity instanceof EntityWitherSkull) {
            return TextFormatting.DARK_GRAY + "Wither skull";
        }
        if (entity instanceof EntityEnderCrystal) {
            return TextFormatting.LIGHT_PURPLE + "End crystal";
        }
        if (entity instanceof EntityEnderPearl) {
            return "Thrown ender pearl";
        }
        if (entity instanceof EntityMinecart) {
            return "Minecart";
        }
        if (entity instanceof EntityItemFrame) {
            return "Item frame";
        }
        if (entity instanceof EntityEgg) {
            return "Thrown egg";
        }
        if (entity instanceof EntitySnowball) {
            return "Thrown snowball";
        }

        return entity.getName();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, Comparator.comparing(o -> (o.getValue())));

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void destroyGUI() {
        kill();
    }

    private static final int DOCK_OFFSET = 0;

    public static void dock(Frame component) {
        Docking docking = component.getDocking();
        if (docking.isTop())
            component.setY(DOCK_OFFSET);
        if (docking.isBottom())
            component.setY((Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight() - DOCK_OFFSET);
        if (docking.isLeft())
            component.setX(DOCK_OFFSET);
        if (docking.isRight())
            component.setX((Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale()) - component.getWidth() - DOCK_OFFSET);
        if (docking.isCenterHorizontal())
            component.setX((Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2));
        if (docking.isCenterVertical())
            component.setY(Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2);

    }

}
