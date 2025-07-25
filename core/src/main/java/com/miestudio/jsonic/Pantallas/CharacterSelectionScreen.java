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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.UIUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla de seleccion de personaje donde los jugadores eligen su personaje antes de iniciar la partida.
 * Muestra los personajes disponibles y su estado (tomado/disponible).
 */
public class CharacterSelectionScreen implements Screen {
    private JuegoSonic game; /** Referencia a la instancia principal del juego. */
    private Stage stage; /** Escenario de Scene2D para la gestion de la UI. */
    private ConcurrentHashMap<String, Boolean> selectedCharacters; /** Mapa que almacena el estado de seleccion de cada personaje. */

    private TextButton sonicButton; /** Boton para seleccionar a Sonic. */
    private TextButton tailsButton; /** Boton para seleccionar a Tails. */
    private TextButton knucklesButton; /** Boton para seleccionar a Knuckles. */

    /**
     * Constructor de CharacterSelectionScreen.
     * @param game La instancia principal del juego.
     * @param selectedCharacters Mapa que contiene el estado de seleccion actual de los personajes.
     */
    public CharacterSelectionScreen(JuegoSonic game, ConcurrentHashMap<String, Boolean> selectedCharacters) {
        this.game = game;
        this.selectedCharacters = selectedCharacters;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    /**
     * Crea y configura la interfaz de usuario para la seleccion de personajes.
     */
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Estilo base para los botones
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont(); // Usar una fuente por defecto.
        buttonStyle.fontColor = Color.WHITE;

        // Crear drawables una sola vez
        Drawable blueDrawable = UIUtils.createColorDrawable(Color.BLUE);
        Drawable grayDrawable = UIUtils.createColorDrawable(Color.GRAY);
        Drawable darkGrayDrawable = UIUtils.createColorDrawable(Color.DARK_GRAY);

        // Estilo para botones habilitados
        TextButton.TextButtonStyle enabledButtonStyle = new TextButton.TextButtonStyle(buttonStyle);
        enabledButtonStyle.up = blueDrawable;
        enabledButtonStyle.down = darkGrayDrawable;

        // Estilo para botones deshabilitados
        TextButton.TextButtonStyle disabledButtonStyle = new TextButton.TextButtonStyle(buttonStyle);
        disabledButtonStyle.up = grayDrawable;
        disabledButtonStyle.fontColor = Color.DARK_GRAY;

        Label titleLabel = new Label("Selecciona tu Personaje", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        table.add(titleLabel).padBottom(50).row();

        sonicButton = new TextButton("Sonic", enabledButtonStyle);
        tailsButton = new TextButton("Tails", enabledButtonStyle);
        knucklesButton = new TextButton("Knuckles", enabledButtonStyle);

        table.add(sonicButton).width(200).height(50).padBottom(20).row();
        table.add(tailsButton).width(200).height(50).padBottom(20).row();
        table.add(knucklesButton).width(200).height(50).row();

        // Listener generico para todos los personajes
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

    /**
     * Actualiza el estado visual de un boton de seleccion de personaje (habilitado/deshabilitado, texto).
     * @param button El TextButton a actualizar.
     * @param characterName El nombre del personaje asociado al boton.
     */
    private void updateButtonState(TextButton button, String characterName) {
        if (game.isCharacterTaken(characterName)) {
            button.setText(characterName + " (Tomado)");
            button.setDisabled(true);
            button.getStyle().up = UIUtils.createColorDrawable(Color.GRAY);
            button.getStyle().fontColor = Color.DARK_GRAY;
        } else {
            button.setText(characterName);
            button.setDisabled(false);
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
