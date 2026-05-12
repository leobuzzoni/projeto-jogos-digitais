package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Main implements ApplicationListener {

    Texture heroTexture;
    Texture heroJumpTexture;

    Texture ceuTexture;
    Texture predios1Texture;
    Texture sombraTexture;
    Texture predios2Texture;
    Texture trilhos1Texture;
    Texture trilhos2Texture;

    Texture obstacleTexture;

    Texture gameoverScreen;

    Texture restartBtn;
    Texture restartBtnHover;

    Rectangle restartRect;

    boolean gameOver = false;

    Sprite heroSprite;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    BitmapFont font;

    float speed = 4f;
    float groundY = 1.5f;

    float velocityY = 0;
    float gravity = -20f;
    float jumpForce = 8f;

    boolean isJumping = false;

    Array<Sprite> obstacles;
    float obstacleTimer = 0;

    Rectangle heroRect;
    Rectangle obstacleRect;

    Vector3 touchPos = new Vector3();

    float offsetCeu = 0;
    float offsetPredios1 = 0;
    float offsetSombra = 0;
    float offsetPredios2 = 0;
    float offsetTrilhos1 = 0;
    float offsetTrilhos2 = 0;

    // SCORE E FASES
    float score = 0;

    float gameSpeed = 2f;
    float spawnRate = 2f;

    int currentPhase = 1;
    String phaseName = "FACIL";

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

        gameoverScreen = new Texture("gameoverScreen.png");

        restartBtn = new Texture("restart.png");
        restartBtnHover = new Texture("restartHover.png");

        spriteBatch = new SpriteBatch();

        viewport = new FitViewport(8, 5);

        // FONTE
        font = new BitmapFont();
        font.getData().setScale(0.03f);

        // PERSONAGEM
        heroSprite = new Sprite(heroTexture);
        heroSprite.setSize(1, 1);
        heroSprite.setPosition(1, groundY);

        // OBSTÁCULOS
        obstacles = new Array<>();

        // COLISÃO
        heroRect = new Rectangle();
        obstacleRect = new Rectangle();

        // BOTÃO RESTART
        restartRect = new Rectangle(3, 0.5f, 2, 0.8f);
    }

    @Override
    public void resize(int width, int height) {

        if(width <= 0 || height <= 0) return;

        viewport.update(width, height, true);
    }

    @Override
    public void render() {

        input();

        logic();

        draw();
    }

    private void input() {

        touchPos.set(
            Gdx.input.getX(),
            Gdx.input.getY(),
            0
        );

        viewport.unproject(touchPos);

        // RESTART
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

            if(gameOver &&
                restartRect.contains(touchPos.x, touchPos.y)) {

                resetGame();
            }
        }

        if(gameOver) return;

        float delta = Gdx.graphics.getDeltaTime();

        // MOVIMENTO
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            heroSprite.translateX(speed * delta);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

            heroSprite.translateX(-speed * delta);
        }

        // PULO
        if((Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP))
            && !isJumping) {

            velocityY = jumpForce;

            isJumping = true;
        }
    }

    private void logic() {

        float delta = Gdx.graphics.getDeltaTime();

        if(!gameOver) {

            float worldWidth = viewport.getWorldWidth();

            // GRAVIDADE
            velocityY += gravity * delta;

            heroSprite.translateY(velocityY * delta);

            // CHÃO
            if(heroSprite.getY() <= groundY) {

                heroSprite.setY(groundY);

                velocityY = 0;

                isJumping = false;
            }

            // TROCA SPRITE
            heroSprite.setTexture(
                isJumping ? heroJumpTexture : heroTexture
            );

            // LIMITE TELA
            heroSprite.setX(MathUtils.clamp(
                heroSprite.getX(),
                0,
                worldWidth - heroSprite.getWidth()
            ));

            // SPAWN
            obstacleTimer += delta;

            if(obstacleTimer > spawnRate) {

                obstacleTimer = 0;

                Sprite obstacle = new Sprite(obstacleTexture);

                obstacle.setSize(1, 1);

                obstacle.setPosition(worldWidth, 1.3f);

                obstacles.add(obstacle);
            }

            // OBSTÁCULOS
            for(int i = obstacles.size - 1; i >= 0; i--) {

                Sprite obstacle = obstacles.get(i);

                obstacle.translateX(-gameSpeed * delta);

                // REMOVE
                if(obstacle.getX() < -1) {

                    obstacles.removeIndex(i);

                    continue;
                }

                // COLISÃO
                heroRect.set(
                    heroSprite.getX(),
                    heroSprite.getY(),
                    0.7f,
                    0.7f
                );

                obstacleRect.set(
                    obstacle.getX(),
                    obstacle.getY(),
                    0.7f,
                    0.7f
                );

                if(heroRect.overlaps(obstacleRect)) {

                    gameOver = true;
                }
            }

            // SCORE
            score += delta * 10;

            // VELOCIDADE PROGRESSIVA
            gameSpeed += delta * 0.03f;

            // FASES
            if(score > 200 && currentPhase == 1) {

                currentPhase = 2;

                phaseName = "MEDIO";

                gameSpeed = 3f;

                spawnRate = 1.5f;
            }

            if(score > 500 && currentPhase == 2) {

                currentPhase = 3;

                phaseName = "DIFICIL";

                gameSpeed = 4f;

                spawnRate = 1f;
            }
        }

        // PARALAXE
        offsetCeu -= 0.1f * delta;
        offsetPredios1 -= 0.3f * delta;
        offsetSombra -= 0.4f * delta;
        offsetPredios2 -= 0.6f * delta;
        offsetTrilhos1 -= 1.5f * delta;
    }

    private void drawLayer(Texture texture, float offset) {

        float w = viewport.getWorldWidth();

        float h = viewport.getWorldHeight();

        float x = offset % w;

        spriteBatch.draw(texture, x, 0, w, h);

        spriteBatch.draw(texture, x + w, 0, w, h);
    }

    private void draw() {

        ScreenUtils.clear(Color.BLACK);

        viewport.apply();

        spriteBatch.setProjectionMatrix(
            viewport.getCamera().combined
        );

        spriteBatch.begin();

        // FUNDO
        drawLayer(ceuTexture, offsetCeu);
        drawLayer(predios1Texture, offsetPredios1);
        drawLayer(sombraTexture, offsetSombra);
        drawLayer(predios2Texture, offsetPredios2);
        drawLayer(trilhos1Texture, offsetTrilhos1);
        drawLayer(trilhos2Texture, offsetTrilhos2);

        // OBSTÁCULOS
        for(Sprite obstacle : obstacles) {

            obstacle.draw(spriteBatch);
        }

        // PERSONAGEM
        heroSprite.draw(spriteBatch);

        // HUD

        // FUNDO DO HUD
        spriteBatch.setColor(0, 0, 0, 0.45f);

        spriteBatch.draw(
            ceuTexture,
            0.15f,
            4.15f,
            2.4f,
            0.75f
        );

        spriteBatch.setColor(Color.WHITE);

        // SCORE
        font.setColor(Color.WHITE);

        font.draw(
            spriteBatch,
            "SCORE: " + (int)score,
            0.25f,
            4.72f
        );

        // TEXTO FASE
        font.draw(
            spriteBatch,
            "FASE:",
            0.25f,
            4.40f
        );

        // COR DA FASE
        if(currentPhase == 1) {

            font.setColor(Color.GREEN);

        } else if(currentPhase == 2) {

            font.setColor(Color.YELLOW);

        } else {

            font.setColor(Color.RED);
        }

        font.draw(
            spriteBatch,
            phaseName,
            1.05f,
            4.40f
        );

        font.setColor(Color.WHITE);

        // GAME OVER
        if(gameOver) {

            float w = viewport.getWorldWidth();

            float h = viewport.getWorldHeight();

            float goWidth = w * 1.0f;

            float goHeight = h * 1.0f;

            float goX = (w - goWidth) / 2;

            float goY = h * 0.2f;

            spriteBatch.draw(
                gameoverScreen,
                goX,
                goY,
                goWidth,
                goHeight
            );

            boolean hover = restartRect.contains(
                touchPos.x,
                touchPos.y
            );

            spriteBatch.draw(
                hover ? restartBtnHover : restartBtn,
                restartRect.x,
                restartRect.y,
                restartRect.width,
                restartRect.height
            );
        }

        spriteBatch.end();
    }

    private void resetGame() {

        gameOver = false;

        obstacles.clear();

        heroSprite.setPosition(1, groundY);

        score = 0;

        gameSpeed = 2f;

        spawnRate = 2f;

        currentPhase = 1;

        phaseName = "FACIL";
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {

        heroTexture.dispose();
        heroJumpTexture.dispose();

        ceuTexture.dispose();
        predios1Texture.dispose();
        sombraTexture.dispose();
        predios2Texture.dispose();
        trilhos1Texture.dispose();
        trilhos2Texture.dispose();

        obstacleTexture.dispose();

        gameoverScreen.dispose();

        restartBtn.dispose();
        restartBtnHover.dispose();

        font.dispose();

        spriteBatch.dispose();
    }
}
