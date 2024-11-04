package com.example.adminapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminapp.api.ServiceCallback;
import com.example.adminapp.api.TagService;
import com.example.adminapp.databinding.UsageGraphsBinding;
import com.example.adminapp.model.Tag;
import com.example.adminapp.model.TagInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UsageGraphFrag extends Fragment {

    private UsageGraphsBinding binding;
    private List<TagInfo> myTaglist;
    private List<String> tag_added_list = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.usage_graphs, container, false);

        updateAllGraphs(rootView);

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        System.out.println(myTaglist);
        if (myTaglist != null) {
            for (TagInfo tag : myTaglist) {
                tag.printTag();
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<Tag> convertJsonToTagList(JsonNode tagJsonArray) {
        List<Tag> tagList = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode tagJson : tagJsonArray) {
            try {
                Tag tag = mapper.treeToValue(tagJson, Tag.class);
                tagList.add(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tagList;
    }

    private List<TagInfo> convertJsonToTagInfoList(JsonNode tagJsonArray) {
        List<TagInfo> tagList = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode tagJson : tagJsonArray) {
            try {
                TagInfo tag = mapper.treeToValue(tagJson, TagInfo.class);
                tagList.add(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tagList;
    }

    private void updateAllGraphs(View rootView) {
        TagService service = new TagService();
        // Find the LineChart view in the layout
        LineChart lineChart = rootView.findViewById(R.id.line_chart_1);
        service.getAllTags(new ServiceCallback() {
               @Override
               public void onSuccess(JsonNode jsonTagList) {
                   List<TagInfo> tagList = new ArrayList<>();
                   for(JsonNode tag : jsonTagList) {
                       tagList.add(new TagInfo(tag));
                   }
                   myTaglist = tagList;
                   

                   if (myTaglist != null) {
                       Calendar todayCalendar = Calendar.getInstance();
                       todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
                       todayCalendar.set(Calendar.MINUTE, 0);
                       todayCalendar.set(Calendar.SECOND, 0);
                       todayCalendar.set(Calendar.MILLISECOND, 0);
                       int numTagsToday = 0;
                       for (TagInfo tag : myTaglist) {
                           Calendar registeredCalendar = Calendar.getInstance();
                           registeredCalendar.setTime(tag.getRegisteredAt());

                           if (registeredCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                   registeredCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                                   registeredCalendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH)) {
                               numTagsToday++;
                           }
                       }
                       TextView numTodayText = rootView.findViewById(R.id.num_registered_text);
                       numTodayText.setText(String.valueOf(numTagsToday));

                       int numTags = myTaglist.size();
                       TextView numTagsText = rootView.findViewById(R.id.num_tags_text);
                       numTagsText.setText(String.valueOf(numTags)); // Convert int to String before setting as text
                       Collections.sort(tagList, new Comparator<TagInfo>() {
                           @Override
                           public int compare(TagInfo tag1, TagInfo tag2) {
                               return Integer.compare(tag1.getTodaysPings(), tag2.getTodaysPings());
                           }
                       });

                       // Create entries for the graph
                       List<Entry> entries = new ArrayList<>();
                       List<String> xValues = new ArrayList<>(); // Store tag names for x-axis
                       for (int i = 0; i < tagList.size(); i++) {
                           TagInfo tag = tagList.get(i);
                           entries.add(new Entry(i, tag.getTodaysPings()));
                           xValues.add(tag.getName()); // Add tag name to xValues
                       }

                       // Create a dataset from the entries
                       LineDataSet dataSet = new LineDataSet(entries, "Todays Pings");
                       dataSet.setColor(Color.BLUE);
                       dataSet.setValueTextColor(Color.RED);

                       // Create a LineData object from the dataset
                       LineData lineData = new LineData(dataSet);

                       // Set the data for the chart
                       lineChart.setData(lineData);

                       LineChart lineChart1 = rootView.findViewById(R.id.line_chart_2);

                    // Customize the description of the chart (optional)
                    Description description = new Description();
                    description.setText("");
                    lineChart.setDescription(description);

                       // Set x-axis values
                       XAxis xAxis = lineChart1.getXAxis();
                       xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
                       xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                       xAxis.setGranularity(1);
                       xAxis.setGranularityEnabled(true);

                       // Refresh the chart
                       lineChart.invalidate();

                       Collections.sort(tagList, new Comparator<TagInfo>() {
                           @Override
                           public int compare(TagInfo tag1, TagInfo tag2) {
                               return Integer.compare(tag1.getTotalPings(), tag2.getTotalPings());
                           }
                       });

                       // Create entries for the graph
                       List<Entry> entries1 = new ArrayList<>();
                       List<String> xValues1 = new ArrayList<>(); // Store tag names for x-axis
                       for (int i = 0; i < tagList.size(); i++) {
                           TagInfo tag = tagList.get(i);
                           entries1.add(new Entry(i, tag.getTotalPings()));
                           xValues1.add(tag.getName()); // Add tag name to xValues
                       }

                       // Create a dataset from the entries
                       LineDataSet dataSet1 = new LineDataSet(entries1, "Total Pings");
                       dataSet1.setColor(Color.BLUE);
                       dataSet1.setValueTextColor(Color.RED);

                       // Create a LineData object from the dataset
                       LineData lineData1 = new LineData(dataSet1);

                       // Set the data for the chart
                       lineChart1.setData(lineData1);

                    // Customize the description of the chart (optional)
                    Description description1 = new Description();
                    description1.setText("");
                    lineChart1.setDescription(description1);

                       // Set x-axis values
                       XAxis xAxis1 = lineChart1.getXAxis();
                       xAxis1.setValueFormatter(new IndexAxisValueFormatter(xValues));
                       xAxis1.setPosition(XAxis.XAxisPosition.BOTTOM);
                       xAxis1.setGranularity(1);
                       xAxis1.setGranularityEnabled(true);

                       // Refresh the chart
                       lineChart1.invalidate();
                   }


                   // Determine the Tag Replacement Date
                   for(int i = 0; i < myTaglist.size();i++){
                       String deviceName = myTaglist.get(i).getDeviceName();
                       String location = myTaglist.get(i).getLocation();
                       Date d = myTaglist.get(i).getLastModified();
                       Calendar calendar = Calendar.getInstance();
                       calendar.setTime(d);

                       // Add 728 days to the calendar
                       calendar.add(Calendar.DAY_OF_YEAR, 728);

                       // Get the new date, which is 728 days from d
                       Date newDate = calendar.getTime();
                       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                       String formattedDate = dateFormat.format(newDate);

                       myTaglist.get(i).setReplacementDate(formattedDate);
                   }

                   // Sort myTaglist based on replacementDate
                   Collections.sort(myTaglist, new Comparator<TagInfo>() {
                       @Override
                       public int compare(TagInfo tag1, TagInfo tag2) {
                           try {
                               SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                               Date date1 = dateFormat.parse(tag1.getReplacementDate());
                               Date date2 = dateFormat.parse(tag2.getReplacementDate());
                               return date1.compareTo(date2);
                           } catch (ParseException e) {
                               // Handle parsing exception
                               e.printStackTrace();
                               return 0; // Default to no change in order
                           }
                       }
                   });

                   RecyclerView recyclerView = rootView.findViewById(R.id.tagBatteryLifeList);
                   TagListAdapter tagListAdapter = new TagListAdapter(requireContext(), myTaglist);
                   recyclerView.setAdapter(tagListAdapter);
                   recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
               }

               @Override
               public void onFailure(Throwable t) {
                   System.out.println(t.toString());
               }
           }
        );
    }

    private int getIcon(String type) {
        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = getResources().getIdentifier(type.toLowerCase() + "_icon", "drawable", requireContext().getPackageName());
        return resourceId;
    }

    private int getIconBlack(String type) {
        // Assuming your resource names follow a naming convention like "type_icon"
        int resourceId = getResources().getIdentifier(type.toLowerCase() + "_icon_black", "drawable", requireContext().getPackageName());

        return resourceId;
    }

    private int getDp(float dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
