package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.ScrollingBackground;
import com.miestudio.jsonic.Util.UIUtils;

/**
 * Pantalla principal del juego que muestra las opciones iniciales al usuario.
 * Permite iniciar el juego, acceder a la ayuda o ver estadisticas.
 */
public class MainScreen implements Screen {

    private final JuegoSonic game; /** Referencia a la instancia principal del juego. */
    private final Stage stage; /** Escenario de Scene2D para la gestion de la UI. */
    private final SpriteBatch batch;
    private final ScrollingBackground background;

    /**
     * Constructor de MainScreen.
     * @param game La instancia principal del juego.
     */
    public MainScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.batch = new SpriteBatch();
        this.background = new ScrollingBackground(Constantes.BACKGROUND_MAINSCREEN_PATH + "Background.png", 50);

        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    /**
     * Configura la interfaz de usuario de la pantalla principal, incluyendo botones y sus listeners.
     */
    private void setupUI() {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.up = UIUtils.createColorDrawable(Color.BLUE);
        buttonStyle.down = UIUtils.createColorDrawable(Color.DARK_GRAY);
        buttonStyle.fontColor = Color.WHITE;

        TextButton playButton = new TextButton("Jugar", buttonStyle);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        playButton.setSize(200, 80);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainScreen", "Boton Jugar presionado. Iniciando NetworkManager.");
                game.networkManager.checkNetworkStatus();
                // Transición a pantalla de selección de personaje
                game.setScreen(new CharacterSelectionScreen(game, game.selectedCharacters));
            }
        });

        TextButton helpButton = new TextButton("Ayuda", buttonStyle);
        helpButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 40);
        helpButton.setSize(200, 80);
        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainScreen", "Boton Ayuda presionado.");
                // Transición a pantalla de ayuda
                //game.setScreen(new HelpScreen(game));
            }
        });

        TextButton statsButton = new TextButton("Estadísticas", buttonStyle);
        statsButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 130);
        statsButton.setSize(200, 80);
        statsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Transición a pantalla de estadísticas
                //game.setScreen(new StatsScreen(game));
            }
        });

        stage.addActor(playButton);
        stage.addActor(helpButton);
        stage.addActor(statsButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.update(delta);
        batch.begin();
        background.render(batch);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        background.updateRenderSize();
        background.reset();
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        stage.dispose();
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
