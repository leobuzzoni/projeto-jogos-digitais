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
    Texture cityTexture;
    Texture obstacleTexture;

    Sprite heroSprite;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    float speed = 4f;
    float groundY = 1.0f;

    Array<Sprite> obstacles;
    float obstacleTimer = 0;

    Rectangle heroRect;
    Rectangle obstacleRect;

    @Override
    public void create() {
        heroTexture = new Texture("hero01.png");
        cityTexture = new Texture("city01.png");
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
        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            heroSprite.translateX(speed * Gdx.graphics.getDeltaTime());
        }

        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            heroSprite.translateX(-speed * Gdx.graphics.getDeltaTime());
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();
        float worldWidth = viewport.getWorldWidth();

        heroSprite.setY(groundY);

        heroSprite.setX(MathUtils.clamp(
            heroSprite.getX(),
            0,
            worldWidth - heroSprite.getWidth()
        ));

        // spawn de obstáculos
        obstacleTimer += delta;
        if(obstacleTimer > 1f) {
            obstacleTimer = 0;

            Sprite obstacle = new Sprite(obstacleTexture);
            obstacle.setSize(1, 1);

            float x = worldWidth; // nasce fora da tela à direita
            obstacle.setPosition(x, groundY);

            obstacles.add(obstacle);
        }

        // atualizar obstáculos (movem para esquerda)
        for(int i = obstacles.size - 1; i >= 0; i--) {
            Sprite obstacle = obstacles.get(i);

            obstacle.translateX(-3f * delta);

            // remove quando sai da tela pela esquerda
            if(obstacle.getX() < -obstacle.getWidth()) {
                obstacles.removeIndex(i);
                continue;
            }

            // colisão
            heroRect.set(
                heroSprite.getX(),
                heroSprite.getY(),
                heroSprite.getWidth(),
                heroSprite.getHeight()
            );

            obstacleRect.set(
                obstacle.getX(),
                obstacle.getY(),
                obstacle.getWidth(),
                obstacle.getHeight()
            );

            if(heroRect.overlaps(obstacleRect)) {
                System.out.println("COLIDIU!");
                obstacles.removeIndex(i);
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        spriteBatch.draw(cityTexture, 0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight());

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
        cityTexture.dispose();
        obstacleTexture.dispose();
        spriteBatch.dispose();
    }
}
