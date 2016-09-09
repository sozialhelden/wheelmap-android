package org.wheelmap.android.tango;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.google.auto.value.AutoValue;

import org.wheelmap.android.online.R;
import org.wheelmap.android.online.databinding.TangoModeSelectionItemBinding;
import org.wheelmap.android.online.databinding.TangoModeSelectionViewBinding;

import java.util.ArrayList;
import java.util.List;

public class ModeSelectionView extends LinearLayout {

    public interface OnItemSelected {
        void onItemSelected(Item item);
    }

    private TangoModeSelectionViewBinding binding;

    private Item selectedItem = null;
    private List<Item> items = new ArrayList<>();

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

    }

    public void setItems(List<Item> items) {
        this.items = items;
        binding.choosableContent.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Item item : items) {
            TangoModeSelectionItemBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.tango_mode_selection_item, binding.choosableContent, true);

            if (item.drawable() != 0) {
                itemBinding.icon.setImageResource(item.drawable());
            }
            itemBinding.title.setText(item.title());

            itemBinding.getRoot().setOnClickListener(v -> {
                setSelectedItem(item);
            });

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

    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    @AutoValue
    public abstract static class Item {
        abstract String title();

        abstract int drawable();

        /**
         * used to identify this item
         */
        abstract Object tag();
    }

}
