package org.wheelmap.android.tango;

import android.animation.LayoutTransition;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.support.transition.TransitionManager;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoModeSelectionItemBinding;
import org.wheelmap.android.online.databinding.TangoModeSelectionViewBinding;

import java.util.ArrayList;
import java.util.List;

public class ModeSelectionView extends LinearLayout {

    public interface OnItemSelectedListener {
        void onItemSelected(Item item);
    }

    private TangoModeSelectionViewBinding binding;

    private Item selectedItem = null;
    private List<Item> items = new ArrayList<>();
    private OnItemSelectedListener listener;

    public ModeSelectionView(Context context) {
        super(context);
        init();
    }

    public ModeSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModeSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.tango_mode_selection_view,
                this,
                true
        );

        LayoutTransition transition = new LayoutTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.enableTransitionType(LayoutTransition.APPEARING);
            transition.enableTransitionType(LayoutTransition.DISAPPEARING);
            transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
            transition.enableTransitionType(LayoutTransition.CHANGING);
        }
        ((ViewGroup) binding.getRoot()).setLayoutTransition(transition);

        binding.choosableContent.getLayoutParams().height = 0;
        binding.top.setOnClickListener(v -> {
            toggleMenu();
        });

    }

    private void toggleMenu() {
        openMenu(binding.choosableContent.getLayoutParams().height == 0);
    }

    public void openMenu(boolean open) {
        int rotation = 0;
        if (!open) {
            rotation = 0;
            binding.choosableContent.getLayoutParams().height = 0;
        } else {
            rotation = 180;
            binding.choosableContent.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        }
        binding.choosableContent.setLayoutParams(binding.choosableContent.getLayoutParams());
        binding.expandButton.animate().rotation(rotation).start();
    }

    public void setItems(List<Item> items) {
        this.items = items;
        binding.choosableContent.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Item item : items) {
            if (item == selectedItem) {
                continue;
            }

            TangoModeSelectionItemBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.tango_mode_selection_item, binding.choosableContent, true);

            if (item.drawable() != 0) {
                itemBinding.icon.setImageResource(item.drawable());
            }
            itemBinding.title.setText(item.title());

            itemBinding.getRoot().setOnClickListener(v -> {
                setSelectedItem(item);
                if (listener != null) {
                    listener.onItemSelected(item);
                }
            });

        }
    }

    public void setSelectedItemTag(Object tag) {
        if (tag == null) {
            return;
        }

        List<Item> items1 = this.items;
        for (int i = 0; i < items1.size(); i++) {
            Item item = items1.get(i);
            if (tag.equals(item.tag())) {
                setSelectedItem(item);
                return;
            }
        }

    }

    public void setSelectedItem(Item item) {

        int selectedItemIndex = -1;
        List<Item> items1 = this.items;
        for (int i = 0; i < items1.size(); i++) {
            Item item1 = items1.get(i);
            if (item1.equals(item)) {
                selectedItemIndex = i;
                break;
            }
        }

        selectedItem = items.get(selectedItemIndex);

        if (item.drawable() != 0) {
            binding.selectedItem.icon.setImageResource(item.drawable());
        }
        binding.selectedItem.title.setText(item.title());

        openMenu(false);

        setItems(items);

    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setOnItemSelectionListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @AutoValue
    public abstract static class Item {

        @StringRes
        abstract int title();

        @DrawableRes
        abstract int drawable();

        /**
         * used to identify this item
         */
        @Nullable
        abstract Object tag();

        public static Item create(@StringRes int title, @DrawableRes int drawable, Object tag) {
            return new AutoValue_ModeSelectionView_Item(title, drawable, tag);
        }
    }

}
