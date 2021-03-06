package com.dutamobile.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dutamobile.R;
import com.dutamobile.model.Status;
import com.dutamobile.net.NetClient;
import com.dutamobile.util.Helper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bartosz on 30.12.13.
 */
public class StatusDialog extends DialogFragment
{
    public static final String CURRENT_DESC = "crntDesc";
    private static final String DESCRIPTIONS = "jsonDescs";
    private Status myStatus, prevStatus;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private AutoCompleteTextView descView;
    private List<String> descs;
    private String prevDesc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.dialog_status_change, null);

        prefs = getActivity().getSharedPreferences(Helper.PREFS_PRIVATE, Activity.MODE_PRIVATE);
        editor = prefs.edit();

        prevStatus = myStatus = Status.valueOf(prefs.getString("status", "AVAILABLE"));
        prevDesc = prefs.getString(CURRENT_DESC, "");

        descView = (AutoCompleteTextView) v.findViewById(R.id.text);
        descView.setThreshold(0);
        setDescriptions();
        descView.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, descs));
        descView.setText(prevDesc);

        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner.setAdapter(new StatusAdapter());
        spinner.setSelection(myStatus.ordinal(), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (myStatus.ordinal() != position)
                    myStatus = Status.values()[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        getDialog().setTitle(getString(R.string.setting_desc));

        return v;
    }

    private void setDescriptions()
    {
        String json = prefs.getString(DESCRIPTIONS, "[]");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        descs = Helper.getGsonInstance().fromJson(json, type);
    }

    @Override
    public void onStop()
    {
        String desc = descView.getText().toString();
        boolean update = ((prevStatus != myStatus) || (!desc.equals(prevDesc)));
        editor.putString("status", myStatus.toString()).putString(CURRENT_DESC, desc);
        if (!descs.contains(desc))
        {
            descs.add(desc);
            int size = descs.size();
            if (size > 10)
                descs = descs.subList(size - 10, size - 1);
            String json = Helper.getGsonInstance().toJson(descs);
            editor.putString(DESCRIPTIONS, json);
        }
        editor.commit();
        if (update)
            NetClient.GetInstance().SetStatus(myStatus, desc);
        super.onStop();
    }

    private class StatusAdapter extends BaseAdapter
    {
        String[] data;

        public StatusAdapter()
        {
            data = getResources().getStringArray(R.array.status_items);
        }

        @Override
        public int getCount()
        {
            if (data != null) return data.length;
            return 0;
        }

        @Override
        public Object getItem(int position)
        {
            if (data != null) return data[position];
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            if (data != null) return data[position].hashCode();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_status, null);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(data[position]);
            imageView.setImageDrawable(Helper.getStatusIndicator(getActivity(), Status.values()[position]));

            return convertView;
        }
    }
}
