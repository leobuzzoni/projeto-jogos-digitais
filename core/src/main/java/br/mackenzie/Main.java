package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {

    private Texture heroTexture;
    private Texture heroJumpTexture;
    private Texture obstacleTexture;
    private Texture ceuTexture;
    private Texture predios1Texture;
    private Texture sombraTexture;
    private Texture predios2Texture;
    private Texture trilhos1Texture;
    private Texture trilhos2Texture;

    private Sprite heroSprite;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;
    private GlyphLayout layout;

    private float speed = 4f;
    private float groundY = 1.5f;
    private float velocityY = 0;
    private float gravity = -20f;
    private float jumpForce = 8f;
    private boolean isJumping = false;
    private boolean isDucking = false;
    private boolean gameOver = false;
    private boolean showMenu = false;
    private boolean modoPedaleira = false;

    private Array<Sprite> obstacles;
    private float obstacleTimer = 0;

    private Rectangle heroRect;
    private Rectangle obstacleRect;
    private Rectangle restartRect = new Rectangle();
    private Rectangle menuBtnRect = new Rectangle();
    private Rectangle btnFacil = new Rectangle();
    private Rectangle btnMedio = new Rectangle();
    private Rectangle btnDificil = new Rectangle();
    private Rectangle btnPedaleira = new Rectangle();

    private Vector3 touchPos = new Vector3();
    private float offsetCeu = 0;
    private float offsetPredios1 = 0;
    private float offsetSombra = 0;
    private float offsetPredios2 = 0;
    private float offsetTrilhos1 = 0;
    private float offsetTrilhos2 = 0;

    private float scoreAcumulado = 0;
    private int score = 0;
    private int highscore = 0;
    private float gameSpeed = 2.5f;
    private float spawnRate = 2f;
    private float multiplicadorPontos = 15f;
    private String phaseName = "FACIL";

    @Override
    public void create() {
        heroTexture = new Texture("hero01.png");
        heroJumpTexture = new Texture("heroJump.png");
        ceuTexture = new Texture("ceu1.png");
        predios1Texture = new Texture("predios2.png");
        sombraTexture = new Texture("sombrapredios3.png");
        predios2Texture = new Texture("predios4.png");
        trilhos1Texture = new Texture("trilhos5.png");
        trilhos2Texture = new Texture("trilhos6.png");
        obstacleTexture = new Texture("obstaculo.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        layout = new GlyphLayout();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ka1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        font.setUseIntegerPositions(false);
        font.getData().setScale(0.012f);

        heroSprite = new Sprite(heroTexture);
        heroSprite.setSize(1, 1);
        heroSprite.setPosition(1, groundY);

        obstacles = new Array<>();
        heroRect = new Rectangle();
        obstacleRect = new Rectangle();
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (gameOver && !showMenu) {
                if (restartRect.contains(touchPos.x, touchPos.y)) resetGame(1);
                if (menuBtnRect.contains(touchPos.x, touchPos.y)) showMenu = true;
            } else if (showMenu) {
                if (btnFacil.contains(touchPos.x, touchPos.y)) resetGame(1);
                if (btnMedio.contains(touchPos.x, touchPos.y)) resetGame(2);
                if (btnDificil.contains(touchPos.x, touchPos.y)) resetGame(3);
                if (btnPedaleira.contains(touchPos.x, touchPos.y)) resetGame(4);
            }
        }

        if (gameOver || showMenu) return;

        float delta = Gdx.graphics.getDeltaTime();

        if (!modoPedaleira) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) heroSprite.translateX(speed * delta);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) heroSprite.translateX(-speed * delta);
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) && !isJumping && !isDucking) {
            velocityY = jumpForce;
            isJumping = true;
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) && !isJumping) {
            isDucking = true;
            heroSprite.setScale(1f, 0.5f);
        } else {
            isDucking = false;
            heroSprite.setScale(1f, 1f);
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        offsetCeu -= 0.1f * delta;
        offsetPredios1 -= 0.3f * delta;
        offsetSombra -= 0.5f * delta;
        offsetPredios2 -= 0.8f * delta;
        offsetTrilhos1 -= gameSpeed * 0.7f * delta;
        offsetTrilhos2 -= 0;

        if (gameOver || showMenu) return;

        velocityY += gravity * delta;
        heroSprite.translateY(velocityY * delta);

        if (heroSprite.getY() <= groundY) {
            heroSprite.setY(groundY);
            velocityY = 0;
            isJumping = false;
        }

        heroSprite.setTexture(isJumping ? heroJumpTexture : heroTexture);
        heroSprite.setX(MathUtils.clamp(heroSprite.getX(), 0, viewport.getWorldWidth() - heroSprite.getWidth()));

        obstacleTimer += delta;
        if (obstacleTimer >= spawnRate) {
            obstacleTimer = 0;
            Sprite obs = new Sprite(obstacleTexture);
            obs.setSize(1, 1);
            obs.setPosition(viewport.getWorldWidth(), 1.3f);
            obstacles.add(obs);
        }

        for (int i = obstacles.size - 1; i >= 0; i--) {
            Sprite obs = obstacles.get(i);
            obs.translateX(-gameSpeed * delta);
            if (obs.getX() < -1) { obstacles.removeIndex(i); continue; }

            float heightScale = isDucking ? 0.4f : 0.7f;
            heroRect.set(heroSprite.getX() + 0.2f, heroSprite.getY() + 0.1f, 0.5f, heightScale);
            obstacleRect.set(obs.getX() + 0.2f, obs.getY() + 0.1f, 0.5f, 0.7f);

            if (heroRect.overlaps(obstacleRect)) {
                gameOver = true;
                if (score > highscore) highscore = score;
            }
        }

        scoreAcumulado += delta * multiplicadorPontos;
        score = (int) scoreAcumulado;
        gameSpeed += delta * 0.1f;

        if (!modoPedaleira) {
            if (score >= 150 && phaseName.equals("FACIL")) aplicarDificuldade(2);
            else if (score >= 400 && phaseName.equals("MEDIO")) aplicarDificuldade(3);
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        drawLayer(ceuTexture, offsetCeu);
        drawLayer(predios1Texture, offsetPredios1);
        drawLayer(sombraTexture, offsetSombra);
        drawLayer(predios2Texture, offsetPredios2);
        drawLayer(trilhos1Texture, offsetTrilhos1);
        drawLayer(trilhos2Texture, offsetTrilhos2);

        for (Sprite obs : obstacles) obs.draw(spriteBatch);
        heroSprite.draw(spriteBatch);

        if (!gameOver && !showMenu) {
            font.draw(spriteBatch, "SCORE " + score + "  |  " + phaseName, 0.3f, 4.8f);
        } else if (showMenu) {
            drawMenuUI();
        } else {
            drawGameOverUI();
        }

        spriteBatch.end();
    }

    private void drawGameOverUI() {
        float w = viewport.getWorldWidth();
        layout.setText(font, "FINAL SCORE: " + score);
        font.draw(spriteBatch, "FINAL SCORE: " + score, (w - layout.width) / 2, 3.8f);

        layout.setText(font, "BEST RECORD: " + highscore);
        font.draw(spriteBatch, "BEST RECORD: " + highscore, (w - layout.width) / 2, 3.2f);

        String rText = "[ RESTART ]";
        layout.setText(font, rText);
        float rX = 1.2f, rY = 1.5f;
        restartRect.set(rX, rY - layout.height, layout.width, layout.height + 0.2f);
        font.setColor(restartRect.contains(touchPos.x, touchPos.y) ? Color.YELLOW : Color.WHITE);
        font.draw(spriteBatch, rText, rX, rY);

        String mText = "[ MENU ]";
        layout.setText(font, mText);
        float mX = 4.5f;
        menuBtnRect.set(mX, rY - layout.height, layout.width, layout.height + 0.2f);
        font.setColor(menuBtnRect.contains(touchPos.x, touchPos.y) ? Color.CYAN : Color.WHITE);
        font.draw(spriteBatch, mText, mX, rY);
        font.setColor(Color.WHITE);
    }

    private void drawMenuUI() {
        float w = viewport.getWorldWidth();
        layout.setText(font, "SELECIONE A DIFICULDADE");
        font.draw(spriteBatch, "SELECIONE A DIFICULDADE", (w - layout.width) / 2, 4.2f);

        drawMenuButton("1. FACIL", 3.4f, btnFacil, Color.GREEN);
        drawMenuButton("2. MEDIO", 2.8f, btnMedio, Color.YELLOW);
        drawMenuButton("3. DIFICIL", 2.2f, btnDificil, Color.RED);
        drawMenuButton("4. PEDALEIRA", 1.6f, btnPedaleira, Color.ORANGE);
    }

    private void drawMenuButton(String text, float y, Rectangle rect, Color hoverColor) {
        layout.setText(font, text);
        float x = (viewport.getWorldWidth() - layout.width) / 2;
        rect.set(x, y - layout.height, layout.width, layout.height + 0.2f);
        font.setColor(rect.contains(touchPos.x, touchPos.y) ? hoverColor : Color.WHITE);
        font.draw(spriteBatch, text, x, y);
        font.setColor(Color.WHITE);
    }

    private void resetGame(int fase) {
        gameOver = false;
        showMenu = false;
        obstacles.clear();
        heroSprite.setPosition(1, groundY);
        velocityY = 0;
        score = 0;
        scoreAcumulado = 0;
        obstacleTimer = 0;
        aplicarDificuldade(fase);
    }

    private void aplicarDificuldade(int fase) {
        modoPedaleira = (fase == 4);

        if (fase == 1) {
            phaseName = "FACIL"; gameSpeed = 2.5f; spawnRate = 2f; multiplicadorPontos = 15f;
        } else if (fase == 2) {
            phaseName = "MEDIO"; gameSpeed = 3.6f; spawnRate = 1.3f; multiplicadorPontos = 35f;
        } else if (fase == 3) {
            phaseName = "DIFICIL"; gameSpeed = 5.2f; spawnRate = 0.7f; multiplicadorPontos = 75f;
        } else if (fase == 4) {
            phaseName = "PEDALEIRA"; gameSpeed = 4.0f; spawnRate = 1.1f; multiplicadorPontos = 50f;
        }
    }

    private void drawLayer(Texture tex, float offset) {
        float w = viewport.getWorldWidth(), h = viewport.getWorldHeight();
        float x = offset % w;
        spriteBatch.draw(tex, x, 0, w, h);
        spriteBatch.draw(tex, x + w, 0, w, h);
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        heroTexture.dispose(); heroJumpTexture.dispose(); ceuTexture.dispose();
        predios1Texture.dispose(); sombraTexture.dispose(); predios2Texture.dispose();
        trilhos1Texture.dispose(); trilhos2Texture.dispose(); obstacleTexture.dispose();
        font.dispose(); spriteBatch.dispose();
    }
}
