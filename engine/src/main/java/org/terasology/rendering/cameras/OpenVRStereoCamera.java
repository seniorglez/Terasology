/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.cameras;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.terasology.math.geom.Quat4f;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.lwjgl.opengl.GL11;
import org.terasology.config.RenderingConfig;
import org.terasology.math.MatrixUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.openvrprovider.OpenVRUtil;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;
import org.terasology.world.WorldProvider;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Camera which can be used to render stereoscopic images of the scene for VR.
 */
public class OpenVRStereoCamera extends SubmersibleCamera {

    private Matrix4f projectionMatrixLeftEye = new Matrix4f();
    private Matrix4f projectionMatrixRightEye = new Matrix4f();

    private Matrix4f inverseProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f inverseProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f inverseViewProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f inverseViewProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f viewMatrixLeftEye = new Matrix4f();
    private Matrix4f viewMatrixRightEye = new Matrix4f();

    private Matrix4f viewMatrixReflectedLeftEye = new Matrix4f();
    private Matrix4f viewMatrixReflectedRightEye = new Matrix4f();

    private ViewFrustum viewFrustumLeftEye = new ViewFrustum();
    private ViewFrustum viewFrustumRightEye = new ViewFrustum();
    private ViewFrustum viewFrustumReflectedLeftEye = new ViewFrustum();
    private ViewFrustum viewFrustumReflectedRightEye = new ViewFrustum();

    private Matrix4f viewProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f viewProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f viewTranslationLeftEye = new Matrix4f();
    private Matrix4f viewTranslationRightEye = new Matrix4f();
    private OpenVRProvider vrProvider;

    public OpenVRStereoCamera(OpenVRProvider provider, WorldProvider worldProvider, RenderingConfig renderingConfig) {
        super(worldProvider, renderingConfig);
        vrProvider = provider;
        // OpenVR's projection matrix is such that this is approximately true.
        zFar = 400.0f;
    }

    @Override
    public void updateFrustum() {
        super.updateFrustum();

        viewFrustumLeftEye.updateFrustum(viewMatrixLeftEye.get(BufferUtils.createFloatBuffer(16)),
            projectionMatrixLeftEye.get(BufferUtils.createFloatBuffer(16)));
        viewFrustumRightEye.updateFrustum(viewMatrixRightEye.get(BufferUtils.createFloatBuffer(16)),
            projectionMatrixRightEye.get(BufferUtils.createFloatBuffer(16)));
        viewFrustumReflectedLeftEye.updateFrustum(viewMatrixReflectedLeftEye.get(BufferUtils.createFloatBuffer(16)),
            projectionMatrixLeftEye.get(BufferUtils.createFloatBuffer(16)));
        viewFrustumReflectedRightEye.updateFrustum(viewMatrixReflectedRightEye.get(BufferUtils.createFloatBuffer(16))
            , projectionMatrixRightEye.get(BufferUtils.createFloatBuffer(16)));
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }

    @Override
    public ViewFrustum getViewFrustum() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewFrustumLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewFrustumRightEye;
        }

        return null;
    }

    @Override
    public ViewFrustum getViewFrustumReflected() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewFrustumReflectedLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewFrustumReflectedRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getViewProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getViewMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            if (!isReflected()) {
                return viewMatrixLeftEye;
            }
            return viewMatrixReflectedLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            if (!isReflected()) {
                return viewMatrixRightEye;
            }
            return viewMatrixReflectedRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return projectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return projectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getInverseProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return inverseProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return inverseProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getInverseViewProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return inverseViewProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return inverseViewProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    @Deprecated
    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(getProjectionMatrix().get(BufferUtils.createFloatBuffer(16)));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    @Deprecated
    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(getViewMatrix().get(BufferUtils.createFloatBuffer(16)));
    }

    @Override
    @Deprecated
    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(normViewMatrix.get(BufferUtils.createFloatBuffer(16)));
    }

    @Override
    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    @Override
    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    @Override
    public void updateMatrices(float fov) {
        prevViewProjectionMatrix.set(viewProjectionMatrix);

        Matrix4f leftEyeProjection = vrProvider.getState().getEyeProjectionMatrix(0);
        Matrix4f rightEyeProjection = vrProvider.getState().getEyeProjectionMatrix(1);
        Matrix4f leftEyePose = vrProvider.getState().getEyePose(0);
        Matrix4f rightEyePose = vrProvider.getState().getEyePose(1);
        float halfIPD = (float) Math.sqrt(Math.pow(leftEyePose.m30() - rightEyePose.m30(), 2)
            + Math.pow(leftEyePose.m31() - rightEyePose.m31(), 2)
            + Math.pow(leftEyePose.m32() - rightEyePose.m32(), 2)) / 2.0f;

        // set camera orientation
        Vector4f vecQuaternion = OpenVRUtil.convertToQuaternion(leftEyePose);
        Quaternionf quaternion = new Quaternionf(vecQuaternion.x, vecQuaternion.y, vecQuaternion.z, vecQuaternion.w);
        setOrientation(new Quat4f(quaternion.x, quaternion.y, quaternion.z, quaternion.w));


        leftEyePose = leftEyePose.invert(); // view matrix is inverse of pose matrix
        rightEyePose = rightEyePose.invert();


        if (Math.sqrt(Math.pow(leftEyePose.m30(), 2) + Math.pow(leftEyePose.m31(), 2) + Math.pow(leftEyePose.m32(),
            2)) < 0.25) {
            return;
        }
        projectionMatrixLeftEye.set(leftEyeProjection);
        projectionMatrixRightEye.set(rightEyeProjection);
        projectionMatrix = projectionMatrixLeftEye;

        viewMatrixLeftEye.set(leftEyePose);
        viewMatrixRightEye.set(rightEyePose);

        viewMatrix = viewMatrixLeftEye;
        normViewMatrix = viewMatrixLeftEye;

        reflectionMatrix.setRow(0, new Vector4f(1.0f, 0.0f, 0.0f, 0.0f));
        reflectionMatrix.setRow(1, new Vector4f(0.0f, -1.0f, 0.0f, 2f * (-position.y + 32f)));
        reflectionMatrix.setRow(2, new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
        reflectionMatrix.setRow(3, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        viewMatrix.mul(reflectionMatrix, viewMatrixReflected);

        reflectionMatrix.setRow(1, new Vector4f(0.0f, -1.0f, 0.0f, 0.0f));
        normViewMatrix.mul(reflectionMatrix, normViewMatrixReflected);

        viewTranslationLeftEye.identity();
        viewTranslationLeftEye.setTranslation(halfIPD, 0.0f, 0.0f);

        viewTranslationRightEye.identity();
        viewTranslationRightEye.setTranslation(-halfIPD, 0.0f, 0.0f);

        viewTranslationLeftEye.mul(viewMatrixReflected, viewMatrixReflectedLeftEye);
        viewTranslationRightEye.mul(viewMatrixReflected, viewMatrixReflectedRightEye);

        projectionMatrixLeftEye.mul(viewMatrixLeftEye, viewProjectionMatrixLeftEye);
        projectionMatrixRightEye.mul(viewMatrixRightEye, viewProjectionMatrixRightEye);

        viewProjectionMatrixLeftEye.invert(inverseViewProjectionMatrixLeftEye);
        viewProjectionMatrixRightEye.invert(inverseViewProjectionMatrixRightEye);

        projectionMatrixLeftEye.invert(inverseProjectionMatrixLeftEye);
        projectionMatrixRightEye.invert(inverseProjectionMatrixRightEye);

        updateFrustum();
    }
}
