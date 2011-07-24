package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;
import com.blork.anpod.util.UIUtils;

// TODO: Auto-generated Javadoc
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape.
 */
public class HomeActivity extends FragmentActivity {
	
	/** The pictures. */
	public static List<Picture> pictures = new ArrayList<Picture>();

	
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //startActivity(new Intent(this, FragmentStatePagerSupport.class));
        setContentView(R.layout.fragment_layout);
        
        UIUtils.setupActionBar(this);
    }
    
}
