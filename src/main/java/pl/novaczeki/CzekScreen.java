package pl.novaczeki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CzekScreen extends Screen {

    private static final long MAX_AMOUNT = 999_999_999_999L; // 12 cyfr

    private TextFieldWidget amountField;

    public CzekScreen() {
        super(Text.literal("Generator Czeków - Anarchia.gg"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 55;

        // Pole tekstowe (tylko cyfry)
        amountField = new TextFieldWidget(this.textRenderer, cx - 100, top + 20, 200, 20, Text.literal("Kwota"));
        amountField.setMaxLength(12);
        amountField.setPlaceholder(Text.literal("Wpisz kwotę czeku..."));
        amountField.setTextPredicate(s -> s.matches("\\d*"));
        this.addDrawableChild(amountField);
        this.setInitialFocus(amountField);

        // Szybkie przyciski
        int bw = 48;
        int gap = 4;
        int rowX = cx - (4 * bw + 3 * gap) / 2;
        int rowY = top + 48;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+1k"), b -> addAmount(1_000))
                .dimensions(rowX, rowY, bw, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+10k"), b -> addAmount(10_000))
                .dimensions(rowX + (bw + gap), rowY, bw, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+100k"), b -> addAmount(100_000))
                .dimensions(rowX + 2 * (bw + gap), rowY, bw, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Wyczyść"), b -> {
                    amountField.setText("");
                    this.setFocused(amountField);
                })
                .dimensions(rowX + 3 * (bw + gap), rowY, bw, 20).build());

        // Główny przycisk
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Wypłać czek"), b -> submit())
                .dimensions(cx - 100, top + 78, 200, 20).build());
    }

    private void addAmount(long delta) {
        long current = 0;
        String text = amountField.getText();
        if (!text.isEmpty()) {
            try {
                current = Long.parseLong(text);
            } catch (NumberFormatException ignored) {
            }
        }
        long result = Math.min(current + delta, MAX_AMOUNT);
        amountField.setText(Long.toString(result));
        this.setFocused(amountField);
    }

    private void submit() {
        String amount = amountField.getText().trim();
        if (amount.isEmpty()) {
            return;
        }
        // usuń zbędne zera z przodu; "000" -> nic nie wysyłamy
        amount = amount.replaceFirst("^0+(?!$)", "");
        if (amount.equals("0")) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand("czek " + amount);
        }
        this.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 55, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false; // multiplayer – gra i tak się nie zatrzymuje
    }
}
