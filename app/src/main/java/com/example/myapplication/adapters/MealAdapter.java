package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Meal;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> meals;
    private List<String> docIds;
    private OnMealClickListener listener;
    private boolean animateAll;

    public MealAdapter(List<Meal> meals, List<String> docIds, OnMealClickListener listener, boolean animateAll) {
        this.meals = meals;
        this.docIds = docIds;
        this.listener = listener;
        this.animateAll = animateAll;
    }

    public void setAnimateAll(boolean animateAll) {
        this.animateAll = animateAll;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.mealNameTextView.setText(meal.getMealName());
        holder.caloriesTextView.setText("kcal: " + meal.getCalories());

        if (animateAll && position == 0) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_right);
            holder.itemView.startAnimation(animation);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public interface OnMealClickListener {
        void onMealClick(int position);
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView mealNameTextView;
        TextView caloriesTextView;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealNameTextView = itemView.findViewById(R.id.mealNameTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
        }
    }
}
