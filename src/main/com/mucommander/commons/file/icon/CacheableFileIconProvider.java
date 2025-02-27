package com.mucommander.commons.file.icon;

import com.mucommander.commons.file.AbstractFile;

import javax.swing.*;
import java.awt.*;

/**
 * <code>CacheableFileIconProvider</code> is an interface to be implemented by file icon providers that wish to use
 * some icon caching to improve performance. This interface is to be used in conjunction with {@link CachedFileIconProvider}
 * to form a functional cached provider.
 *
 * @author Maxence Bernard
 */
public interface CacheableFileIconProvider extends FileIconProvider {

    /**
     * Returns <code>true</code> if the icon cache can be used for the specified file and preferred resolution. This
     * method allows the icon cache to be used only for certain types of files and/or preferred resolutions.
     *
     * <p>This method is called by {@link CachedFileIconProvider#getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * each time an icon is requested. If <code>true</code> is returned, the icon cache will be looked up with
     * {@link #lookupCache(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)} and if the cache did not return
     * an icon, the icon will be added to the cache with {@link #addToCache(com.mucommander.commons.file.AbstractFile, javax.swing.Icon, java.awt.Dimension)}.
     * <br>
     * On the other hand, if <code>false</code> is returned, {@link #getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * will simply be called, without querying or adding to the cache.
     *
     * @param file the file for which to retrieve an icon
     * @param preferredResolution the preferred resolution for the icon
     * @return true if the icon cache can be used with the specified file
     */
    boolean isCacheable(AbstractFile file, Dimension preferredResolution);

    /**
     * This method is called by {@link CachedFileIconProvider#getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * to perform a cache lookup and give implementations a chance to re-use a cached icon. If a non-null value is
     * returned, the returned icon will be used.
     * <br>
     * On the other hand, if <code>null</code> is returned, the icon will be fetched using {@link #getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * followed by a call to {@link #addToCache(com.mucommander.commons.file.AbstractFile, javax.swing.Icon,java.awt.Dimension)}
     * to add the freshly-retrieved icon to the cache.
     *
     * <p>This method is called only if the prior call to {@link #isCacheable(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * returned <code>true</code>.
     *
     * @param file the file for which to look for a previously cached icon
     * @param preferredResolution the preferred resolution for the icon
     * @return a cached icon to re-use, null if there is none
     */
    Icon lookupCache(AbstractFile file, Dimension preferredResolution);

    /**
     * This method is called by {@link CachedFileIconProvider#getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * to give implementations a chance to cache an icon fetched with {@link #getFileIcon(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * and have it returned later by {@link #lookupCache(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}.
     * <br>
     * There is no obligation for this method to cache the given icon, implementations may freely choose whether to
     * cache certain icons only.
     *
     * <p>This method is called only if the prior call to {@link #isCacheable(com.mucommander.commons.file.AbstractFile, java.awt.Dimension)}
     * returned <code>true</code>.
     *
     * @param file the file that corresponds to the given icon
     * @param icon the icon to add to the cache
     * @param preferredResolution the preferred icon resolution that was originally requested
     */
    void addToCache(AbstractFile file, Icon icon, Dimension preferredResolution);

}
