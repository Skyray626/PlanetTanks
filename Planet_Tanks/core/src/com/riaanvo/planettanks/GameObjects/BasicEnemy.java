/*
 * Copyright (C) 2017 Riaan Van Onselen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.riaanvo.planettanks.GameObjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.riaanvo.planettanks.Physics.Collider;
import com.riaanvo.planettanks.Physics.SphereCollider;
import com.riaanvo.planettanks.managers.CollisionManager;
import com.riaanvo.planettanks.managers.GameObjectManager;
import com.riaanvo.planettanks.managers.LevelManager;

import java.util.LinkedList;

/**
 * Created by riaanvo on 25/5/17.
 */

public class BasicEnemy extends LivingGameObject {
    private TankController mTankController;

    private CollisionManager mCollisionManager;
    private SphereCollider mBaseSphereCollider;


    private SphereCollider mSearchSphereCollider;
    private Vector3 fireDirection;
    private boolean playerInRange;
    private boolean aimingAtPlayer;
    private boolean clearLineOfFire;

    private float aimingSpeed;
    private float aimingThreshold;
    private float mMinTimeBetweenShots;
    private float mShotTimer;

    private int pointsOnKilled;

    public BasicEnemy(TankController tankController) {
        super();
        mTankController = tankController;
        mTankController.setParent(this);

        mCollisionManager = CollisionManager.get();
        float colliderRadius = 0.8f;
        Vector3 colliderOffset = new Vector3(0, 0.5f, 0);
        mBaseSphereCollider = new SphereCollider(this, Collider.ColliderTag.ENTITIES, colliderOffset, colliderRadius);
        mCollisionManager.addCollider(mBaseSphereCollider);
        mSearchSphereCollider = new SphereCollider(this, Collider.ColliderTag.ENTITIES, colliderOffset, 10f);

        setTag("BasicEnemy");
        setHealth(1);

        mMinTimeBetweenShots = 1f;
        mShotTimer = 0;
        aimingSpeed = 90f;
        aimingThreshold = 5f;

        pointsOnKilled = 100;

        playerInRange = false;
        aimingAtPlayer = false;
        clearLineOfFire = true;
    }


    @Override
    public void update(float dt) {
        if (isDead()) {
            handelDeath();
            return;
        }

        mShotTimer += dt;


        if (playerInRange = isPlayerInRange()) {
            rotateToTarget(dt);
            if (aimingAtPlayer = isAimingAtPlayer()) {
                clearLineOfFire = true;
            }
        }

        if (playerInRange && aimingAtPlayer && clearLineOfFire) {
            if (mShotTimer > mMinTimeBetweenShots) {
                mShotTimer = 0;
                mTankController.shoot();
            }
        }

    }

    private boolean isPlayerInRange() {
        LinkedList<Collider> collisions = mCollisionManager.getCollisions(mSearchSphereCollider, Collider.ColliderTag.ENTITIES);
        for (Collider collider : collisions) {
            if (collider.getGameObject().compareTag("Player")) {
                fireDirection = collider.getGameObject().getPosition().cpy().sub(getPosition()).nor();
                return true;
            }
        }
        return false;
    }

    private void rotateToTarget(float deltaTime) {
        float desiredOrientation = calculateOrientation(fireDirection);
        float changeInOrientation = desiredOrientation - mTankController.getTurretOrientation();
        if (Math.abs(changeInOrientation) > 180) changeInOrientation *= -1;
        if (!isAimingAtPlayer()) {
            changeInOrientation /= Math.abs(changeInOrientation);
            changeInOrientation *= aimingSpeed * deltaTime;
        } else {
            changeInOrientation /= 10;
        }

        float newOrientation = mTankController.getTurretOrientation() + changeInOrientation;
        mTankController.setTurretOrientation(newOrientation);
    }

    private boolean isAimingAtPlayer() {
        float changeInOrientation = calculateOrientation(fireDirection) - mTankController.getTurretOrientation();
        return Math.abs(changeInOrientation) < aimingThreshold;
    }

    @Override
    public void render(SpriteBatch spriteBatch, ModelBatch modelBatch) {
        mTankController.renderTank(spriteBatch, modelBatch);

        mCollisionManager.renderCollider(mBaseSphereCollider);
        mCollisionManager.renderCollider(mSearchSphereCollider);
    }

    @Override
    protected void handelDeath() {
        if (!deathHandled) {
            GameObjectManager.get().removeGameObject(this);
            mCollisionManager.removeCollider(mBaseSphereCollider);
            deathHandled = true;
            LevelManager.get().EnemyKilled(pointsOnKilled);
        }
    }

    @Override
    public void setPosition(Vector3 position) {
        mPosition = position.cpy();
        mTankController.setPosition(position.cpy());
        mBaseSphereCollider.updatePosition();
        mSearchSphereCollider.updatePosition();
    }

    public void setTarget(LivingGameObject target) {
        //mTarget = target;
    }

}
