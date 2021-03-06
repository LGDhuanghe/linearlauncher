/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.support.v17.leanback.R;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.animation.TimeAnimator;
import android.content.res.Resources;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_NONE;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_LARGE;

/**
 * Setup the behavior how to highlight when a item gains focus.
 */
public class FocusHighlightHelper {

    static class FocusAnimator implements TimeAnimator.TimeListener {
        private final View mView;
        private final int mDuration;
        private final ShadowOverlayContainer mWrapper;
        private final float mScaleDiff;
        private float mFocusLevel = 0f;
        private float mFocusLevelStart;
        private float mFocusLevelDelta;
        private final TimeAnimator mAnimator = new TimeAnimator();
        private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

        void animateFocus(boolean select, boolean immediate) {
            endAnimation();
            final float end = select ? 1 : 0;
            if (immediate) {
                setFocusLevel(end);
            } else if (mFocusLevel != end) {
                mFocusLevelStart = mFocusLevel;
                mFocusLevelDelta = end - mFocusLevelStart;
                mAnimator.start();
            }
        }

        FocusAnimator(View view, float scale, boolean useDimmer, int duration) {
            mView = view;
            mDuration = duration;
            mScaleDiff = scale - 1f;
            if (view instanceof ShadowOverlayContainer) {
                mWrapper = (ShadowOverlayContainer) view;
            } else {
                mWrapper = null;
            }
            mAnimator.setTimeListener(this);

        }

        void setFocusLevel(float level) {
            mFocusLevel = level;
            float scale = 1f + mScaleDiff * level;
            mView.setScaleX(scale);
            mView.setScaleY(scale);
            if (mWrapper != null) {
                mWrapper.setShadowFocusLevel(level);
            }
        }

        float getFocusLevel() {
            return mFocusLevel;
        }

        void endAnimation() {
            mAnimator.end();
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= mDuration) {
                fraction = 1;
                mAnimator.end();
            } else {
                fraction = (float) (totalTime / (double) mDuration);
            }
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            setFocusLevel(mFocusLevelStart + fraction * mFocusLevelDelta);
        }
    }
    
    public static class DefaultItemFocusHighlight implements FocusHighlightHandler{

        private static final int DURATION_MS = 150;

        private static float[] sScaleFactor = new float[2];
        private static final float SCALE_VALUE = 1.18f;

        private int mScaleIndex;
        private final boolean mUseDimmer;

        public DefaultItemFocusHighlight(int zoomIndex, boolean useDimmer) {
            mScaleIndex = (zoomIndex >= 0 && zoomIndex < sScaleFactor.length) ?
                    zoomIndex : ZOOM_FACTOR_LARGE;
            mUseDimmer = useDimmer;
        }

        private static void lazyInit(Resources resources) {
            if (sScaleFactor[ZOOM_FACTOR_NONE] == 0f) {
                sScaleFactor[ZOOM_FACTOR_NONE] = 1f;
                sScaleFactor[ZOOM_FACTOR_LARGE] = SCALE_VALUE;
            }
        }

        private float getScale(View view) {
            lazyInit(view.getResources());
            return sScaleFactor[mScaleIndex];
        }

        @Override
        public void onItemFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            getOrCreateAnimator(view).animateFocus(hasFocus, false);
        }

        @Override
        public void onInitializeView(View view) {
            getOrCreateAnimator(view).animateFocus(false, true);
        }

        private FocusAnimator getOrCreateAnimator(View view) {
            FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusAnimator(view, getScale(view), mUseDimmer, DURATION_MS);
                view.setTag(R.id.lb_focus_animator, animator);
            }
            return animator;
        }
        
    }
    
    static class BrowseItemFocusHighlight implements FocusHighlightHandler {
        private static final int DURATION_MS = 150;

        private static float[] sScaleFactor = new float[2];
        private static final float SCALE_VALUE = 1.18f;
        private int mScaleIndex;
        private final boolean mUseDimmer;

        BrowseItemFocusHighlight(int zoomIndex, boolean useDimmer) {
            mScaleIndex = (zoomIndex >= 0 && zoomIndex < sScaleFactor.length) ?
                    zoomIndex : ZOOM_FACTOR_LARGE;
            mUseDimmer = useDimmer;
        }

        private static void lazyInit(Resources resources) {
            if (sScaleFactor[ZOOM_FACTOR_NONE] == 0f) {
                sScaleFactor[ZOOM_FACTOR_NONE] = 1f;
                sScaleFactor[ZOOM_FACTOR_LARGE] = SCALE_VALUE;
            }
        }

        private float getScale(View view) {
            lazyInit(view.getResources());
            return sScaleFactor[mScaleIndex];
        }

        @Override
        public void onItemFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            getOrCreateAnimator(view).animateFocus(hasFocus, false);
        }

        @Override
        public void onInitializeView(View view) {
            getOrCreateAnimator(view).animateFocus(false, true);
        }

        private FocusAnimator getOrCreateAnimator(View view) {
            FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusAnimator(view, getScale(view), mUseDimmer, DURATION_MS);
                view.setTag(R.id.lb_focus_animator, animator);
            }
            return animator;
        }

    }

    /**
     * Setup the focus highlight behavior of a focused item in browse list row.
     * @param zoomIndex
     * {@link FocusHighlight#ZOOM_FACTOR_LARGE} {@link FocusHighlight#ZOOM_FACTOR_NONE}.
     * @param useDimmer Allow dimming browse item when unselected.
     * @param adapter  adapter of the list row.
     */
    public static void setupBrowseItemFocusHighlight(ItemBridgeAdapter adapter, int zoomIndex,
            boolean useDimmer) {
        adapter.setFocusHighlight(new BrowseItemFocusHighlight(zoomIndex, useDimmer));
    }
}
