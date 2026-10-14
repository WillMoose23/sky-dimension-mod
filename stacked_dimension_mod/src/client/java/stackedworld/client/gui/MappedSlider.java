package stackedworld.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class MappedSlider extends AbstractSliderButton {
	private final String translationKey;
	private final double min;
	private final double max;

	public MappedSlider(int width, String translationKey, String tooltipKey, double min, double max, double current, double defaultMapped) {
		super(0, 0, width, 20, Component.empty(), fraction(min, max, current));
		this.translationKey = translationKey;
		this.min = min;
		this.max = max;
		this.setTooltip(Tooltip.create(Component.translatable(tooltipKey, percent(min, max, defaultMapped))));
		this.updateMessage();
	}

	public double mapped() {
		return this.min + this.value * (this.max - this.min);
	}

	public void setMapped(double mapped) {
		this.value = fraction(this.min, this.max, mapped);
		this.updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Component.translatable(this.translationKey, percent(this.min, this.max, this.mapped())));
	}

	@Override
	protected void applyValue() {
	}

	private static double fraction(double min, double max, double mapped) {
		return (mapped - min) / (max - min);
	}

	private static int percent(double min, double max, double mapped) {
		int value = (int)Math.round(fraction(min, max, mapped) * 100.0);
		if (value < 0) {
			return 0;
		}
		if (value > 100) {
			return 100;
		}
		return value;
	}
}
