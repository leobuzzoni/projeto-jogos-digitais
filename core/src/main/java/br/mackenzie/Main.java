package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {

    // Texturas
    Texture heroTexture;
    Texture cityTexture;

    // Sprite
    Sprite heroSprite;

    // Renderização
    SpriteBatch spriteBatch;
    FitViewport viewport;

    // Velocidade (simula pedal)
    float speed = 4f;

    @Override
    public void create() {
        heroTexture = new Texture("hero01.png");
        cityTexture = new Texture("city01.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        heroSprite = new Sprite(heroTexture);
        heroSprite.setSize(1, 1);
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

    }

    private void logic() {    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // Fundo
        spriteBatch.draw(cityTexture, 0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight());

        // Personagem
        heroSprite.draw(spriteBatch);

        spriteBatch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        heroTexture.dispose();
        cityTexture.dispose();
        spriteBatch.dispose();
    }
}
