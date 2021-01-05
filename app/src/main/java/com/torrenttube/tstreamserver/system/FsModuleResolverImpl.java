/*
 * Copyright (C) 2019 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of LibreTorrent.
 *
 * LibreTorrent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreTorrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreTorrent.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.torrenttube.tstreamserver.system;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;


class FsModuleResolverImpl implements FsModuleResolver
{
    private Context appContext;
    private SafFsModule safModule;
    private DefaultFsModule defaultModule;

    public FsModuleResolverImpl(@NonNull Context appContext)
    {
        this.appContext = appContext;
        this.safModule = new SafFsModule(appContext);
        this.defaultModule = new DefaultFsModule(appContext);
    }

    @Override
    public FsModule resolveFsByUri(@NonNull Uri uri)
    {
        if (isSafPath(appContext, uri))
            return safModule;
        else if (isFileSystemPath(uri))
            return defaultModule;
        else
            throw new IllegalArgumentException("Cannot resolve file system for the given uri: " + uri);
    }

    public static boolean isSafPath(@NonNull Context appContext, @NonNull Uri path)
    {
        return SafFileSystem.getInstance(appContext).isSafPath(path);
    }

    public static boolean isFileSystemPath(@NonNull Uri path)
    {
        String scheme = path.getScheme();
        if (scheme == null)
            throw new IllegalArgumentException("Scheme of " + path.getPath() + " is null");

        return scheme.equals(ContentResolver.SCHEME_FILE);
    }

}
