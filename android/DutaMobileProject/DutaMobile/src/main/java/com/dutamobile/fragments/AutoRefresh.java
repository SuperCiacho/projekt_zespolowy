package com.dutamobile.fragments;

import com.dutamobile.model.Message;

import java.util.List;

/**
 * Created by Bartosz on 17.11.13.
 */
public interface AutoRefresh
{
    void RefreshView(List<?> data);
}
