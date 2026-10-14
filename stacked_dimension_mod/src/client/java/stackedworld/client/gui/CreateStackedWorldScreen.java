package stackedworld.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import stackedworld.worldgen.SkyIslandSettings;

public final class CreateStackedWorldScreen extends Screen {
	private static final int SLIDER_WIDTH = 310;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 33);
	private final Screen parent;
	private final java.util.function.Consumer<SkyIslandSettings> apply;
	private MappedSlider landFrequency;
	private MappedSlider landSize;
	private MappedSlider oceanFrequency;
	private MappedSlider oceanSize;

	public CreateStackedWorldScreen(Screen parent, SkyIslandSettings current, java.util.function.Consumer<SkyIslandSettings> apply) {
		super(Component.translatable("stackedworld.customize.title"));
		this.parent = parent;
		this.apply = apply;
		SkyIslandSettings defaults = SkyIslandSettings.DEFAULT;
		this.landFrequency = new MappedSlider(
			SLIDER_WIDTH, "stackedworld.customize.land_frequency", "stackedworld.customize.land_frequency.tooltip", 0.15, 0.70, current.landFrequency(), defaults.landFrequency()
		);
		this.landSize = new MappedSlider(
			SLIDER_WIDTH, "stackedworld.customize.land_size", "stackedworld.customize.land_size.tooltip", 0.40, 0.80, current.landSize(), defaults.landSize()
		);
		this.oceanFrequency = new MappedSlider(
			SLIDER_WIDTH,
			"stackedworld.customize.ocean_frequency",
			"stackedworld.customize.ocean_frequency.tooltip",
			0.50,
			1.20,
			current.oceanFrequency(),
			defaults.oceanFrequency()
		);
		this.oceanSize = new MappedSlider(
			SLIDER_WIDTH, "stackedworld.customize.ocean_size", "stackedworld.customize.ocean_size.tooltip", 0.60, 1.20, current.oceanSize(), defaults.oceanSize()
		);
	}

	@Override
	protected void init() {
		LinearLayout header = this.layout.addToHeader(LinearLayout.vertical());
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(this.getTitle(), this.font));

		LinearLayout contents = this.layout.addToContents(LinearLayout.vertical().spacing(6));
		contents.defaultCellSetting().alignHorizontallyCenter();
		contents.addChild(this.landFrequency);
		contents.addChild(this.landSize);
		contents.addChild(this.oceanFrequency);
		contents.addChild(this.oceanSize);

		LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footer.addChild(Button.builder(Component.translatable("stackedworld.customize.defaults"), button -> this.restoreDefaults()).width(150).build());
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.apply.accept(this.current());
			this.onClose();
		}).build());
		footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	private void restoreDefaults() {
		SkyIslandSettings defaults = SkyIslandSettings.DEFAULT;
		this.landFrequency.setMapped(defaults.landFrequency());
		this.landSize.setMapped(defaults.landSize());
		this.oceanFrequency.setMapped(defaults.oceanFrequency());
		this.oceanSize.setMapped(defaults.oceanSize());
	}

	private SkyIslandSettings current() {
		return new SkyIslandSettings(
			(float)this.landFrequency.mapped(),
			(float)this.landSize.mapped(),
			(float)this.oceanFrequency.mapped(),
			(float)this.oceanSize.mapped()
		);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}
}
