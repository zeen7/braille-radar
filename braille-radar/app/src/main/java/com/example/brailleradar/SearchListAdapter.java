package com.example.brailleradar;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brailleradar.models.SearchListItem;

import java.util.ArrayList;
import java.util.List;


public class SearchListAdapter extends ArrayAdapter<SearchListItem> {
    private List<SearchListItem> tagListFull;

    public SearchListAdapter(@NonNull Context context, @NonNull List<SearchListItem> tagList) {
        super(context, 0, tagList);
        tagListFull = new ArrayList<>(tagList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return tagFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.search_list_row, parent, false
            );
        }

        TextView tagNameTextView = convertView.findViewById(R.id.search_tag_name);
        ImageView tagIconImageView = convertView.findViewById(R.id.search_icon);
        TextView distanceTextView = convertView.findViewById(R.id.search_distance);
        TextView otherInfoTextView = convertView.findViewById(R.id.search_tag_other_info);

        SearchListItem tagListItem = getItem(position);

        if (tagListItem != null) {
            tagNameTextView.setText(tagListItem.getTagName());
            tagIconImageView.setImageResource(tagListItem.getIconImage());
            distanceTextView.setText(tagListItem.getDistance());
            otherInfoTextView.setText(tagListItem.getOtherInfo());
        }

        return convertView;
    }

    private Filter tagFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<SearchListItem> suggestions = new ArrayList<>();
            // Nothing typed, show all
            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(tagListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (SearchListItem item : tagListFull) {
                    if (item.getTagName().toLowerCase().contains(filterPattern) || item.getOtherInfo().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((SearchListItem) resultValue).getTagName();
        }
    };
}
