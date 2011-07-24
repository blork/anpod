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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * A <em>really</em> dumb implementation of the {@link Menu} interface, that's only useful for our
 * old-actionbar purposes. See <code>com.android.internal.view.menu.MenuBuilder</code> in AOSP for
 * a more complete implementation.
 */
public class SimpleMenu implements Menu {

    /** The m context. */
    private Context mContext;
    
    /** The m resources. */
    private Resources mResources;

    /** The m items. */
    private ArrayList<SimpleMenuItem> mItems;

    /**
     * Instantiates a new simple menu.
     *
     * @param context the context
     */
    public SimpleMenu(Context context) {
        mContext = context;
        mResources = context.getResources();
        mItems = new ArrayList<SimpleMenuItem>();
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Gets the resources.
     *
     * @return the resources
     */
    public Resources getResources() {
        return mResources;
    }

    /* (non-Javadoc)
     * @see android.view.Menu#add(java.lang.CharSequence)
     */
    public MenuItem add(CharSequence title) {
        return addInternal(0, 0, title);
    }

    /* (non-Javadoc)
     * @see android.view.Menu#add(int)
     */
    public MenuItem add(int titleRes) {
        return addInternal(0, 0, mResources.getString(titleRes));
    }

    /* (non-Javadoc)
     * @see android.view.Menu#add(int, int, int, java.lang.CharSequence)
     */
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return addInternal(itemId, order, title);
    }

    /* (non-Javadoc)
     * @see android.view.Menu#add(int, int, int, int)
     */
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return addInternal(itemId, order, mResources.getString(titleRes));
    }

    /**
     * Adds an item to the menu.  The other add methods funnel to this.
     *
     * @param itemId the item id
     * @param order the order
     * @param title the title
     * @return the menu item
     */
    private MenuItem addInternal(int itemId, int order, CharSequence title) {
        final SimpleMenuItem item = new SimpleMenuItem(this, itemId, order, title);
        mItems.add(findInsertIndex(mItems, order), item);
        return item;
    }

    /**
     * Find insert index.
     *
     * @param items the items
     * @param order the order
     * @return the int
     */
    private static int findInsertIndex(ArrayList<? extends MenuItem> items, int order) {
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItem item = items.get(i);
            if (item.getOrder() <= order) {
                return i + 1;
            }
        }

        return 0;
    }

    /**
     * Find item index.
     *
     * @param id the id
     * @return the int
     */
    public int findItemIndex(int id) {
        final int size = size();

        for (int i = 0; i < size; i++) {
            SimpleMenuItem item = mItems.get(i);
            if (item.getItemId() == id) {
                return i;
            }
        }

        return -1;
    }

    /* (non-Javadoc)
     * @see android.view.Menu#removeItem(int)
     */
    public void removeItem(int itemId) {
        removeItemAtInt(findItemIndex(itemId));
    }

    /**
     * Removes the item at int.
     *
     * @param index the index
     */
    private void removeItemAtInt(int index) {
        if ((index < 0) || (index >= mItems.size())) {
            return;
        }
        mItems.remove(index);
    }

    /* (non-Javadoc)
     * @see android.view.Menu#clear()
     */
    public void clear() {
        mItems.clear();
    }

    /* (non-Javadoc)
     * @see android.view.Menu#findItem(int)
     */
    public MenuItem findItem(int id) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            SimpleMenuItem item = mItems.get(i);
            if (item.getItemId() == id) {
                return item;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see android.view.Menu#size()
     */
    public int size() {
        return mItems.size();
    }

    /* (non-Javadoc)
     * @see android.view.Menu#getItem(int)
     */
    public MenuItem getItem(int index) {
        return mItems.get(index);
    }

    // Unsupported operations.

    /* (non-Javadoc)
     * @see android.view.Menu#addSubMenu(java.lang.CharSequence)
     */
    public SubMenu addSubMenu(CharSequence charSequence) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#addSubMenu(int)
     */
    public SubMenu addSubMenu(int titleRes) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#addSubMenu(int, int, int, java.lang.CharSequence)
     */
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#addSubMenu(int, int, int, int)
     */
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#addIntentOptions(int, int, int, android.content.ComponentName, android.content.Intent[], android.content.Intent, int, android.view.MenuItem[])
     */
    public int addIntentOptions(int i, int i1, int i2, ComponentName componentName,
            Intent[] intents, Intent intent, int i3, MenuItem[] menuItems) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#removeGroup(int)
     */
    public void removeGroup(int i) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#setGroupCheckable(int, boolean, boolean)
     */
    public void setGroupCheckable(int i, boolean b, boolean b1) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#setGroupVisible(int, boolean)
     */
    public void setGroupVisible(int i, boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#setGroupEnabled(int, boolean)
     */
    public void setGroupEnabled(int i, boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#hasVisibleItems()
     */
    public boolean hasVisibleItems() {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#close()
     */
    public void close() {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#performShortcut(int, android.view.KeyEvent, int)
     */
    public boolean performShortcut(int i, KeyEvent keyEvent, int i1) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#isShortcutKey(int, android.view.KeyEvent)
     */
    public boolean isShortcutKey(int i, KeyEvent keyEvent) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#performIdentifierAction(int, int)
     */
    public boolean performIdentifierAction(int i, int i1) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    /* (non-Javadoc)
     * @see android.view.Menu#setQwertyMode(boolean)
     */
    public void setQwertyMode(boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }
}
