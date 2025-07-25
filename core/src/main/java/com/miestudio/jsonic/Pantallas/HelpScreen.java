package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.UIUtils;

public class HelpScreen implements Screen {

    private final JuegoSonic game;
    private final Stage stage;
    private Texture backgroundTexture;
    private Texture titleBackgroundTexture;
    private Texture controlsImage;
    private Texture objectivesImage;
    private Texture charactersImage;
    private SpriteBatch batch;

    public HelpScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.batch = new SpriteBatch();
        
        // Cargar texturas
        backgroundTexture = new Texture(Gdx.files.internal(Constantes.BACKGROUND_HELP_PATH + "HelpBackGround.png"));
        titleBackgroundTexture = new Texture(Gdx.files.internal(Constantes.BACKGROUND_HELP_PATH + "Start.png"));
        controlsImage = new Texture(Gdx.files.internal(Constantes.BACKGROUND_HELP_PATH + "Controls.png"));
        objectivesImage = new Texture(Gdx.files.internal(Constantes.BACKGROUND_HELP_PATH + "Basura.png"));
        charactersImage = new Texture(Gdx.files.internal(Constantes.BACKGROUND_HELP_PATH + "Personajes.png"));
        
        setupUI();
    }

    private void setupUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        
        // Configuración de fuentes
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f);
        titleFont.setColor(Color.YELLOW);
        
        BitmapFont sectionFont = new BitmapFont();
        sectionFont.getData().setScale(1.4f);
        sectionFont.setColor(Color.CYAN);
        
        BitmapFont contentFont = new BitmapFont();
        contentFont.setColor(Color.WHITE);

        // Título principal con fondo
        Image titleBg = new Image(titleBackgroundTexture);
        Label titleLabel = new Label("AYUDA DEL JUEGO", new Label.LabelStyle(titleFont, Color.WHITE));
        titleLabel.setAlignment(Align.center);
        
        Table titleTable = new Table();
        titleTable.add(titleBg).size(500, 80);
        titleTable.add(titleLabel).padLeft(10);
        mainTable.add(titleTable).colspan(2).padBottom(30).row();

        // Sección: Personajes
        mainTable.add(new Label("PERSONAJES", new Label.LabelStyle(sectionFont, Color.WHITE)))
                .align(Align.left).padBottom(10).colspan(2).row();
        
        Table charactersTable = new Table();
        charactersTable.add(new Image(charactersImage)).size(300, 150).padRight(20);
        charactersTable.add(new Label(
                "Sonic: Recorre el mapa a gran velocidad\n" +
                "Tails: Puede Volar e invocar robots para limpiar\n" +
                "Knuckles: Rompe monstañas de basura con su increible fuerza\n\n" +
                "Cada personaje tiene habilidades únicas\ny debe cooperar con los demás para superar\nlos desafíos del juego.",
                new Label.LabelStyle(contentFont, Color.WHITE)
        ));
        mainTable.add(charactersTable).colspan(2).padBottom(30).row();

        // Sección: Controles
        mainTable.add(new Label("CONTROLES", new Label.LabelStyle(sectionFont, Color.WHITE)))
                .align(Align.left).padBottom(10).colspan(2).row();
        
        Table controlsTable = new Table();
        controlsTable.add(new Label(
                "Movimiento: Teclas W y D para los movimientos laterales\n y W para saltar (Manten presionado W para que Tails vuele)\n" +
                "Habilidad especial: Tecla E\n" ,
                new Label.LabelStyle(contentFont, Color.WHITE)
        )).padRight(20);
        controlsTable.add(new Image(controlsImage)).size(250, 150);
        mainTable.add(controlsTable).colspan(2).padBottom(30).row();

        // Sección: Objetivos
        mainTable.add(new Label("OBJETIVOS", new Label.LabelStyle(sectionFont, Color.WHITE)))
                .align(Align.left).padBottom(10).colspan(2).row();
        
        Table objectivesTable = new Table();
        objectivesTable.add(new Image(objectivesImage)).size(150, 150).padRight(20);
        objectivesTable.add(new Label(
                "1. Recoger toda la basura para terminar el nivel\n" +
                "2. Evitar que el Dr.Egman siga contaminando\n" +
                "3. Limpiar la contaminación de las islas\n" +
                "4. Cooperar con otros jugadores en línea\n\n" +
                "Cada nivel tiene desafíos únicos y\njefes finales que requieren trabajo en equipo.",
                new Label.LabelStyle(contentFont, Color.WHITE)
        ));
        mainTable.add(objectivesTable).colspan(2).padBottom(40).row();

        // Botón de regreso
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle();
        backButtonStyle.font = new BitmapFont();
        backButtonStyle.up = UIUtils.createColorDrawable(Color.RED);
        backButtonStyle.down = UIUtils.createColorDrawable(Color.DARK_GRAY);
        
        TextButton backButton = new TextButton("Volver al Menú", backButtonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainScreen(game));
            }
        });
        
        mainTable.add(backButton).size(250, 70).colspan(2);
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Dibujar fondo
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 
                                      Gdx.graphics.getWidth(), 
                                      Gdx.graphics.getHeight());
        batch.end();
        
        // Dibujar UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        titleBackgroundTexture.dispose();
        controlsImage.dispose();
        objectivesImage.dispose();
        charactersImage.dispose();
        batch.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
