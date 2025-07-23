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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.UIUtils;

public class CharacterSelectionScreen implements Screen {
    private JuegoSonic game;
    private Stage stage;

    public CharacterSelectionScreen(JuegoSonic game) {
        this.game = game;
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

        TextButton sonicButton = new TextButton("Sonic", buttonStyle);
        table.add(sonicButton).width(200).height(50).padBottom(20).row();

        TextButton tailsButton = new TextButton("Tails", buttonStyle);
        table.add(tailsButton).width(200).height(50).padBottom(20).row();

        TextButton knucklesButton = new TextButton("Knuckles", buttonStyle);
        table.add(knucklesButton).width(200).height(50).row();

        // Listener genérico para todos los personajes
        sonicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("CharacterSelectionScreen", "Sonic seleccionado");
                game.networkManager.sendCharacterSelection("Sonic");
                // Ejemplo: transición a LobbyScreen (puedes adaptar el nombre)
                // game.setScreen(new LobbyScreen(game));
            }
        });

        tailsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("CharacterSelectionScreen", "Tails seleccionado");
                game.networkManager.sendCharacterSelection("Tails");
                // game.setScreen(new LobbyScreen(game));
            }
        });

        knucklesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("CharacterSelectionScreen", "Knuckles seleccionado");
                game.networkManager.sendCharacterSelection("Knuckles");
                // game.setScreen(new LobbyScreen(game));
            }
        });
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
