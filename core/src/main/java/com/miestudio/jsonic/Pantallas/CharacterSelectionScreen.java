package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.UIUtils;

import java.util.concurrent.ConcurrentHashMap;

public class CharacterSelectionScreen implements Screen {
    private JuegoSonic game;
    private Stage stage;
    private ConcurrentHashMap<String, Boolean> selectedCharacters;

    private TextButton sonicButton;
    private TextButton tailsButton;
    private TextButton knucklesButton;

    public CharacterSelectionScreen(JuegoSonic game, ConcurrentHashMap<String, Boolean> selectedCharacters) {
        this.game = game;
        this.selectedCharacters = selectedCharacters;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Estilo base para los botones
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont(); // Usar una fuente por defecto.
        buttonStyle.up = UIUtils.createColorDrawable(Color.BLUE);
        buttonStyle.down = UIUtils.createColorDrawable(Color.DARK_GRAY);
        buttonStyle.fontColor = Color.WHITE;

        Label titleLabel = new Label("Selecciona tu Personaje", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        table.add(titleLabel).padBottom(50).row();

        sonicButton = new TextButton("Sonic", buttonStyle);
        tailsButton = new TextButton("Tails", buttonStyle);
        knucklesButton = new TextButton("Knuckles", buttonStyle);

        table.add(sonicButton).width(200).height(50).padBottom(20).row();
        table.add(tailsButton).width(200).height(50).padBottom(20).row();
        table.add(knucklesButton).width(200).height(50).row();

        // Listener genérico para todos los personajes
        sonicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (sonicButton.isDisabled()) return;
                Gdx.app.log("CharacterSelectionScreen", "Sonic seleccionado");
                game.networkManager.sendCharacterSelection("Sonic");
            }
        });

        tailsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (tailsButton.isDisabled()) return;
                Gdx.app.log("CharacterSelectionScreen", "Tails seleccionado");
                game.networkManager.sendCharacterSelection("Tails");
            }
        });

        knucklesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (knucklesButton.isDisabled()) return;
                game.networkManager.sendCharacterSelection("Knuckles");
            }
        });
    }

    private void updateButtonState(TextButton button, String characterName) {
        if (game.isCharacterTaken(characterName)) {
            button.setText(characterName + " (Tomado)");
            button.setDisabled(true);
            // Cambiar el color del drawable a gris
            ((TextureRegionDrawable) button.getStyle().up).getRegion().getTexture().dispose(); // Liberar la textura anterior
            button.getStyle().up = UIUtils.createColorDrawable(Color.GRAY);
            button.getStyle().fontColor = Color.DARK_GRAY;
        } else {
            button.setText(characterName);
            button.setDisabled(false);
            // Restaurar el color original del drawable
            ((TextureRegionDrawable) button.getStyle().up).getRegion().getTexture().dispose(); // Liberar la textura anterior
            button.getStyle().up = UIUtils.createColorDrawable(Color.BLUE);
            button.getStyle().fontColor = Color.WHITE;
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar el estado de los botones en cada renderizado
        updateButtonState(sonicButton, "Sonic");
        updateButtonState(tailsButton, "Tails");
        updateButtonState(knucklesButton, "Knuckles");

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
