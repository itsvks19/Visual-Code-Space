package com.raredev.vcspace.editor.completion;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.android.material.R;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.raredev.vcspace.editor.databinding.LayoutCompletionListBinding;
import io.github.rosemoe.sora.widget.component.CompletionLayout;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class CustomCompletionLayout implements CompletionLayout {
  private EditorAutoCompletion mEditorAutoCompletion;

  private LayoutCompletionListBinding binding;

  @Override
  public void setEditorCompletion(EditorAutoCompletion completion) {
    mEditorAutoCompletion = completion;
  }

  @Override
  public View inflate(Context context) {
    binding = LayoutCompletionListBinding.inflate(LayoutInflater.from(context));
    binding.getRoot().setBackground(applyBackground(context));

    setLoading(true);
    binding.listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          try {
            mEditorAutoCompletion.select(position);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    return binding.getRoot();
  }

  @Override
  public void setEnabledAnimation(boolean enabledAnimation) {
    if (enabledAnimation) {
      var transition = new LayoutTransition();
      transition.enableTransitionType(LayoutTransition.CHANGING);
      transition.enableTransitionType(LayoutTransition.APPEARING);
      transition.enableTransitionType(LayoutTransition.DISAPPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
      transition.addTransitionListener(
          new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {}

            @Override
            public void endTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {
              if (view != binding.listView) {
                return;
              }
              view.requestLayout();
            }
          });
      binding.getRoot().setLayoutTransition(transition);
      binding.listView.setLayoutTransition(transition);
    } else {
      binding.getRoot().setLayoutTransition(null);
      binding.listView.setLayoutTransition(null);
    }
  }

  @Override
  public void onApplyColorScheme(EditorColorScheme colorScheme) {
    // no
  }

  @Override
  public void setLoading(boolean state) {
    binding.progress.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
  }

  @Override
  public ListView getCompletionList() {
    return binding.listView;
  }

  private void performScrollList(int offset) {
    var adpView = getCompletionList();

    long down = SystemClock.uptimeMillis();
    var ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0, 0, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();
  }

  @Override
  public void ensureListPositionVisible(int position, int increment) {
    binding.listView.post(
        () -> {
          while (binding.listView.getFirstVisiblePosition() + 1 > position && binding.listView.canScrollList(-1)) {
            performScrollList(increment / 2);
          }
          while (binding.listView.getLastVisiblePosition() - 1 < position && binding.listView.canScrollList(1)) {
            performScrollList(-increment / 2);
          }
        });
  }

  private GradientDrawable applyBackground(Context context) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    drawable.setCornerRadius(18);
    drawable.setColor(SurfaceColors.SURFACE_0.getColor(context));
    drawable.setStroke(2, MaterialColors.getColor(context, R.attr.colorOutline, 0));
    return drawable;
  }
}
