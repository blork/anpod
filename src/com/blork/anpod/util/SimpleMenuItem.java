/*
 * Copyright 2011 Google Inc.
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

package com.blork.anpod.util;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * A <em>really</em> dumb implementation of the {@link MenuItem} interface, that's only useful for
 * our old-actionbar purposes. See <code>com.android.internal.view.menu.MenuItemImpl</code> in
 * AOSP for a more complete implementation.
 */
public class SimpleMenuItem implements MenuItem {

    /** The m menu. */
    private SimpleMenu mMenu;

    /** The m id. */
    private final int mId;
    
    /** The m order. */
    private final int mOrder;
    
    /** The m title. */
    private CharSequence mTitle;
    
    /** The m title condensed. */
    private CharSequence mTitleCondensed;
    
    /** The m icon drawable. */
    private Drawable mIconDrawable;
    
    /** The m icon res id. */
    private int mIconResId = 0;
    
    /** The m enabled. */
    private boolean mEnabled = true;

    /**
     * Instantiates a new simple menu item.
     *
     * @param menu the menu
     * @param id the id
     * @param order the order
     * @param title the title
     */
    public SimpleMenuItem(SimpleMenu menu, int id, int order, CharSequence title) {
        mMenu = menu;
        mId = id;
        mOrder = order;
        mTitle = title;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getItemId()
     */
    public int getItemId() {
        return mId;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getOrder()
     */
    public int getOrder() {
        return mOrder;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setTitle(java.lang.CharSequence)
     */
    public MenuItem setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setTitle(int)
     */
    public MenuItem setTitle(int titleRes) {
        return setTitle(mMenu.getContext().getString(titleRes));
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getTitle()
     */
    public CharSequence getTitle() {
        return mTitle;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setTitleCondensed(java.lang.CharSequence)
     */
    public MenuItem setTitleCondensed(CharSequence title) {
        mTitleCondensed = title;
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getTitleCondensed()
     */
    public CharSequence getTitleCondensed() {
        return mTitleCondensed != null ? mTitleCondensed : mTitle;
    }

   /* (non-Javadoc)
    * @see android.view.MenuItem#setIcon(android.graphics.drawable.Drawable)
    */
   public MenuItem setIcon(Drawable icon) {
        mIconResId = 0;
        mIconDrawable = icon;
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setIcon(int)
     */
    public MenuItem setIcon(int iconResId) {
        mIconDrawable = null;
        mIconResId = iconResId;
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getIcon()
     */
    public Drawable getIcon() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        }

        if (mIconResId != 0) {
            return mMenu.getResources().getDrawable(mIconResId);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setEnabled(boolean)
     */
    public MenuItem setEnabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#isEnabled()
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    // No-op operations. We use no-ops to allow inflation from menu XML.

    /* (non-Javadoc)
     * @see android.view.MenuItem#getGroupId()
     */
    public int getGroupId() {
        return 0;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getActionView()
     */
    public View getActionView() {
        return null;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setIntent(android.content.Intent)
     */
    public MenuItem setIntent(Intent intent) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getIntent()
     */
    public Intent getIntent() {
        return null;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setShortcut(char, char)
     */
    public MenuItem setShortcut(char c, char c1) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setNumericShortcut(char)
     */
    public MenuItem setNumericShortcut(char c) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getNumericShortcut()
     */
    public char getNumericShortcut() {
        return 0;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setAlphabeticShortcut(char)
     */
    public MenuItem setAlphabeticShortcut(char c) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getAlphabeticShortcut()
     */
    public char getAlphabeticShortcut() {
        return 0;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setCheckable(boolean)
     */
    public MenuItem setCheckable(boolean b) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#isCheckable()
     */
    public boolean isCheckable() {
        return false;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setChecked(boolean)
     */
    public MenuItem setChecked(boolean b) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#isChecked()
     */
    public boolean isChecked() {
        return false;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setVisible(boolean)
     */
    public MenuItem setVisible(boolean b) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#isVisible()
     */
    public boolean isVisible() {
        return true;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#hasSubMenu()
     */
    public boolean hasSubMenu() {
        return false;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getSubMenu()
     */
    public SubMenu getSubMenu() {
        return null;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setOnMenuItemClickListener(android.view.MenuItem.OnMenuItemClickListener)
     */
    public MenuItem setOnMenuItemClickListener(
            OnMenuItemClickListener onMenuItemClickListener) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#getMenuInfo()
     */
    public ContextMenu.ContextMenuInfo getMenuInfo() {
        return null;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setShowAsAction(int)
     */
    public void setShowAsAction(int i) {
        // Noop
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setActionView(android.view.View)
     */
    public MenuItem setActionView(View view) {
        // Noop
        return this;
    }

    /* (non-Javadoc)
     * @see android.view.MenuItem#setActionView(int)
     */
    public MenuItem setActionView(int i) {
        // Noop
        return this;
    }

}
