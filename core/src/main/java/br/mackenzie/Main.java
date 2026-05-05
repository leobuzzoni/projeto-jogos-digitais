package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Rectangle;

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

    Sprite heroSprite;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    float speed = 4f;
    float groundY = 1.5f;

    // PULO
    float velocityY = 0;
    float gravity = -20f;
    float jumpForce = 8f;
    boolean isJumping = false;

    Array<Sprite> obstacles;
    float obstacleTimer = 0;

    Rectangle heroRect;
    Rectangle obstacleRect;

    // Parallax
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

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        heroSprite = new Sprite(heroTexture);
        heroSprite.setSize(1, 1);
        heroSprite.setPosition(1, groundY);

        obstacles = new Array<>();

        heroRect = new Rectangle();
        obstacleRect = new Rectangle();
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
        float delta = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            heroSprite.translateX(speed * delta);
        }

        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            heroSprite.translateX(-speed * delta);
        }

        //  PULO
        if((Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP))
            && !isJumping) {

            velocityY = jumpForce;
            isJumping = true;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();
        float worldWidth = viewport.getWorldWidth();

        // Gravidade
        velocityY += gravity * delta;
        heroSprite.translateY(velocityY * delta);

        if(heroSprite.getY() <= groundY) {
            heroSprite.setY(groundY);
            velocityY = 0;
            isJumping = false;
        }

        // Troca sprite
        if(isJumping) {
            heroSprite.setTexture(heroJumpTexture);
        } else {
            heroSprite.setTexture(heroTexture);
        }

        // Limite horizontal
        heroSprite.setX(MathUtils.clamp(
            heroSprite.getX(),
            0,
            worldWidth - heroSprite.getWidth()
        ));

        // Parallax
        offsetCeu -= 0.1f * delta;
        offsetPredios1 -= 0.3f * delta;
        offsetSombra -= 0.4f * delta;
        offsetPredios2 -= 0.6f * delta;
        offsetTrilhos1 -= 1.5f * delta;

        // Spawn obstáculos
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

            if(obstacle.getX() < -obstacle.getWidth()) {
                obstacles.removeIndex(i);
                continue;
            }

            
            float heroWidth = heroSprite.getWidth() * 0.7f;
            float heroHeight = heroSprite.getHeight() * 0.7f;

            heroRect.set(
                heroSprite.getX() + (heroSprite.getWidth() - heroWidth) / 2,
                heroSprite.getY() + (heroSprite.getHeight() - heroHeight) / 2,
                heroWidth,
                heroHeight
            );

            
            float obsWidth = obstacle.getWidth() * 0.7f;
            float obsHeight = obstacle.getHeight() * 0.7f;

            obstacleRect.set(
                obstacle.getX() + (obstacle.getWidth() - obsWidth) / 2,
                obstacle.getY() + (obstacle.getHeight() - obsHeight) / 2,
                obsWidth,
                obsHeight
            );

            if(heroRect.overlaps(obstacleRect)) {
                System.out.println("COLIDIU!");
                obstacles.removeIndex(i);
            }
        }
    }

    private void drawLayer(Texture texture, float offset) {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float x = offset % worldWidth;

        spriteBatch.draw(texture, x, 0, worldWidth, worldHeight);
        spriteBatch.draw(texture, x + worldWidth, 0, worldWidth, worldHeight);
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

        for(Sprite obstacle : obstacles) {
            obstacle.draw(spriteBatch);
        }

        heroSprite.draw(spriteBatch);

        spriteBatch.end();
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
        spriteBatch.dispose();
    }
}
