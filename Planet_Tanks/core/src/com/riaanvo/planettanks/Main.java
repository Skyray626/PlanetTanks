package com.riaanvo.planettanks;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.riaanvo.planettanks.managers.CollisionManager;
import com.riaanvo.planettanks.managers.ContentManager;
import com.riaanvo.planettanks.managers.GameStateManager;
import com.riaanvo.planettanks.states.MainMenuState;

public class Main extends ApplicationAdapter {
    private ModelBatch mModelBatch;
    private SpriteBatch mSpriteBatch;
    private GameStateManager mGameStateManager;
    private ContentManager mContentManager;

    private boolean isLoaded;
    private BitmapFont debugFont;

    @Override
    public void create() {
        mModelBatch = new ModelBatch();
        mSpriteBatch = new SpriteBatch();
        mGameStateManager = GameStateManager.get();
        mContentManager = ContentManager.get();

        //For frame rate debugging
        mContentManager.loadSkin(Constants.SKIN_KEY);
        mContentManager.loadTexture(Constants.BLACK_TEXTURE); // used for transitions

        isLoaded = false;

        //Initialise the first screen
        //mGameStateManager.push(new SplashScreenState());
        mGameStateManager.push(new MainMenuState());
        //mGameStateManager.push(new LevelSelectState());
        //mGameStateManager.push(new PlayState());
        //mGameStateManager.push(new LevelEditorState());
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        mGameStateManager.render(mSpriteBatch, mModelBatch);

        if (!isLoaded) {
            if (mContentManager.assetManagerUpdate()) {
                loaded();
            }
            return;
        }

        mSpriteBatch.begin();
        debugFont.draw(mSpriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
        mSpriteBatch.end();

    }

    public void update(float deltaTime) {
        mGameStateManager.update(deltaTime);
    }

    private void loaded() {
        isLoaded = true;
        Skin skin = mContentManager.getSkin(Constants.SKIN_KEY);
        debugFont = skin.getFont(Constants.DEFAULT_FONT);
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        mModelBatch.dispose();
        mSpriteBatch.dispose();
        mGameStateManager.dispose();
        mContentManager.dispose();
        CollisionManager.get().dispose();
    }
}
