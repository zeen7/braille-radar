package com.example.adminapp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminapp.R;
import com.example.adminapp.model.TagInfo;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder> {

    private List<TagInfo> mTagList;
    private LayoutInflater mInflater;
    private Context context;

    TagListAdapter(Context context, List<TagInfo> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mTagList = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.battery_tag_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TagInfo tag = mTagList.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView deviceNameTextView;
        TextView batteryLifeTextView;
        TextView batteryLifeDaysTextView;
        LinearLayout clustersLayout;
        TextView locationTextView;

        ViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.batteryTagImageView);
            nameTextView = itemView.findViewById(R.id.batteryNameTextView);
            deviceNameTextView = itemView.findViewById(R.id.batteryDeviceNameTextView);
            batteryLifeTextView = itemView.findViewById(R.id.batteryLifeTextView);
            batteryLifeDaysTextView = itemView.findViewById(R.id.batteryLifeDaysTextView);
            clustersLayout = itemView.findViewById(R.id.batteryClustersLayout);
            locationTextView = itemView.findViewById(R.id.batteryLocationTextView);
        }

        void bind(TagInfo tag) {
            iconImageView.setImageResource(getIconBlack(tag.getType()));
            iconImageView.setContentDescription(tag.getType());

            final int iconMaxHeight = getDp(50);
            iconImageView.setAdjustViewBounds(true);
            iconImageView.setMaxHeight(iconMaxHeight);

            nameTextView.setText(tag.getName());
            nameTextView.setContentDescription(tag.getName());

            batteryLifeTextView.setText("(" + tag.getReplacementDate() + ")");
            batteryLifeTextView.setContentDescription(tag.getReplacementDate());

            // Parse the replacementDate string into a Date object
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date replacementDate = null;
            try {
                replacementDate = simpleDateFormat.parse(tag.getReplacementDate());
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing exception as needed
            }
            // Get today's date
            Date today = new Date();
            long daysInBetween = getDaysInBetweenDates(today, replacementDate);
            batteryLifeDaysTextView.setText(daysInBetween + " days");
            batteryLifeDaysTextView.setText(daysInBetween + " days");

            deviceNameTextView.setText(tag.getDeviceName());
            deviceNameTextView.setContentDescription(tag.getDeviceName());

            String location = tag.getLocation();
            if (location != null) {
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText("(" + location + ")");
                locationTextView.setContentDescription("at " + location);
            }

            // Clear previous cluster TextViews and add new ones
            clustersLayout.removeAllViews();
            String clustersDescription = "In";
            boolean first = true;
            for (String cluster : tag.getClusterNames()) {
                TextView clusterTextView = new TextView(itemView.getContext());
                clusterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                clusterTextView.setTextColor(Color.DKGRAY);
                clusterTextView.setMaxHeight(getDp(20));
                clusterTextView.setText(cluster);
                clustersDescription += (first ? "" : " and ") + cluster + ", ";
                if (first) {
                    first = false;
                }
                clustersLayout.addView(clusterTextView);
            }
            clustersLayout.setContentDescription(clustersDescription);
        }
    }

    private int getIcon(String type) {
//        ImageView imageView = new ImageView(requireContext());

        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = context.getResources().getIdentifier(type.toLowerCase() + "_icon", "drawable", context.getPackageName());

//        if(resourceId != 0) {
//            imageView.setImageResource(resourceId);
//        }

//        //setting image position
//        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));

//        switch(type) {
//            case "room": imageView.setImageResource(R.drawable.room_icon);
//            break;
//            case "washroom": imageView.setImageResource(R.drawable.washroom_icon);
//            break;
//            case "food": imageView.setImageResource(R.drawable.food_icon);
//            break;
//            case "elevator": imageView.setImageResource(R.drawable.elevator_icon);
//            break;
//            case "stairs": imageView.setImageResource(R.drawable.stairs_icon);
//            break;
//            case "entrance": imageView.setImageResource(R.drawable.entrance_icon);
//            break;
//
//        }

//        return imageView;
        return resourceId;
    }

    private int getIconBlack(String type) {
//        ImageView imageView = new ImageView(requireContext());

        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = context.getResources().getIdentifier(type.toLowerCase() + "_icon_black", "drawable", context.getPackageName());

//        if (resourceId != 0) {
//            imageView.setImageResource(resourceId);
//        }

//        return imageView;
        return resourceId;
    }

    private int getDp(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private long getDaysInBetweenDates(Date start, Date end) {
        // Calculate the difference in milliseconds
        long differenceInMillis = end.getTime() - start.getTime();

        // Calculate the difference in days
        long daysDifference = differenceInMillis / (24 * 60 * 60 * 1000);

        return daysDifference;
    }
}
