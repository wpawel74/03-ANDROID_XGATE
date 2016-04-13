package w_pawel74.a03_xgate;

import android.graphics.drawable.AnimationDrawable;

/**
 * Created by wisniewskip on 2016-04-05.
 */
public class MyAnimationDrawable extends AnimationDrawable {

    public MyAnimationDrawable(AnimationDrawable aniDrawable) {
        /* Add each frame to our animation drawable */
        for (int i = 0; i < aniDrawable.getNumberOfFrames(); i++) {
            this.addFrame(aniDrawable.getFrame(i), aniDrawable.getDuration(i));
        }
    }

    public interface IAnimationFinishListener {
        void onAnimationFinished();
    }

    private boolean finished = false;
    private IAnimationFinishListener animationFinishListener;

    public IAnimationFinishListener getAnimationFinishListener() {
        return animationFinishListener;
    }

    public void setAnimationFinishListener(IAnimationFinishListener animationFinishListener) {
        this.animationFinishListener = animationFinishListener;
    }

    @Override
    public boolean selectDrawable(int idx) {
        boolean ret = super.selectDrawable(idx);

        if ((idx != 0) && (idx == getNumberOfFrames() - 1)) {
            if (!finished) {
                finished = true;
                if (animationFinishListener != null) animationFinishListener.onAnimationFinished();
            }
        }

        return ret;
    }
}
