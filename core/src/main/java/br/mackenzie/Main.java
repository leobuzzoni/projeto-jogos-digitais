package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
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

    @Override
    public void create() {
        heroTexture = new Texture("hero01.png");
        heroJumpTexture = new Texture("heroJump.png");

        ceuTexture = new Texture("céu1.png");
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

        heroSprite = new Sprite(heroTexture);
        heroSprite.setSize(1, 1);
        heroSprite.setPosition(1, groundY);

        obstacles = new Array<>();

        heroRect = new Rectangle();
        obstacleRect = new Rectangle();

        
        restartRect = new Rectangle(3, 0.5f, 2, 0.8f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if(gameOver && restartRect.contains(touchPos.x, touchPos.y)) {
                resetGame();
            }
        }

        if(gameOver) return;

        float delta = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            heroSprite.translateX(speed * delta);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            heroSprite.translateX(-speed * delta);
        }

        if((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP))
            && !isJumping) {

            velocityY = jumpForce;
            isJumping = true;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        if(!gameOver) {

            float worldWidth = viewport.getWorldWidth();

            velocityY += gravity * delta;
            heroSprite.translateY(velocityY * delta);

            if(heroSprite.getY() <= groundY) {
                heroSprite.setY(groundY);
                velocityY = 0;
                isJumping = false;
            }

            heroSprite.setTexture(isJumping ? heroJumpTexture : heroTexture);

            heroSprite.setX(MathUtils.clamp(
                heroSprite.getX(),
                0,
                worldWidth - heroSprite.getWidth()
            ));

            obstacleTimer += delta;
            if(obstacleTimer > 1f) {
                obstacleTimer = 0;
                Sprite obstacle = new Sprite(obstacleTexture);
                obstacle.setSize(1, 1);
                obstacle.setPosition(worldWidth, 1.3f);
                obstacles.add(obstacle);
            }

            for(int i = obstacles.size - 1; i >= 0; i--) {
                Sprite obstacle = obstacles.get(i);

                obstacle.translateX(-3f * delta);

                if(obstacle.getX() < -1) {
                    obstacles.removeIndex(i);
                    continue;
                }

                heroRect.set(heroSprite.getX(), heroSprite.getY(), 0.7f, 0.7f);
                obstacleRect.set(obstacle.getX(), obstacle.getY(), 0.7f, 0.7f);

                if(heroRect.overlaps(obstacleRect)) {
                    gameOver = true;
                }
            }
        }

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
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        drawLayer(ceuTexture, offsetCeu);
        drawLayer(predios1Texture, offsetPredios1);
        drawLayer(sombraTexture, offsetSombra);
        drawLayer(predios2Texture, offsetPredios2);
        drawLayer(trilhos1Texture, offsetTrilhos1);
        drawLayer(trilhos2Texture, offsetTrilhos2);

        for(Sprite obstacle : obstacles) obstacle.draw(spriteBatch);
        heroSprite.draw(spriteBatch);

        if(gameOver) {
            float w = viewport.getWorldWidth();
            float h = viewport.getWorldHeight();

            
            float goWidth = w * 1.0f;
            float goHeight = h * 1.0f;
            float goX = (w - goWidth) / 2;
            float goY = h * 0.2f;

            spriteBatch.draw(gameoverScreen, goX, goY, goWidth, goHeight);

            boolean hover = restartRect.contains(touchPos.x, touchPos.y);

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
    }

    @Override public void pause() {}
    @Override public void resume() {}

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
        spriteBatch.dispose();
    }
}
