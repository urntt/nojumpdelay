package com.urntt.nojumpdelay.config;

import com.urntt.nojumpdelay.NoJumpDelayClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Configuration screen built from vanilla widgets.
 */
public final class NoJumpDelayConfigScreen extends Screen {
	private final Screen parent;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public NoJumpDelayConfigScreen(final Screen parent) {
		super(Component.translatable("options.nojumpdelay.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		NoJumpDelayConfig config = NoJumpDelayClient.config();

		this.layout.addTitleHeader(this.title, this.font);
		this.layout.addToContents(CycleButton.onOffBuilder(config.isEnabled())
				.create(NoJumpDelayClient.FEATURE_NAME, (button, enabled) -> config.setEnabled(enabled)));
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
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
